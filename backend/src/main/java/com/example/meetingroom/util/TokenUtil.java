package com.example.meetingroom.util;

import com.example.meetingroom.common.LoginUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

/**
 * JWT 工具：签发与校验标准三段式令牌（RFC 7519），签名算法固定 HS256（RFC 7518）。
 *
 * 结构：Base64Url(header) + "." + Base64Url(payload) + "." + Base64Url(HMAC-SHA256(前两段))。
 * 签名只保证令牌没有被篡改，不保证载荷里的 username/role 此刻仍然成立。
 * 载荷中的 username/role 只是给前端和调试看的冗余信息，服务端授权的唯一依据是sub（用户 ID）加上拦截器回查数据库得到的最新 status/role，见 AuthInterceptor。
 * 令牌不加密，payload 任何人都能 Base64Url 解码，因此不得放入密码等敏感信息。
 */
@Component
public class TokenUtil {

    /** 写进 header.alg，校验时强制比对，拒绝 none 等其它算法 */
    private static final String JWT_ALG = "HS256";

    /** 仅标识令牌类型，不参与校验 */
    private static final String JWT_TYPE = "JWT";

    /** 签名算法，与 header 里的 alg 取值对应 */
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /** 令牌三段的连接符 */
    private static final char SEPARATOR = '.';

    /** Base64Url 编码器，不带补位符 */
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    /** Base64Url 解码器 */
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    /**
     * 专用的 ObjectMapper：令牌 JSON 只有字符串与整数，不需要 JavaTimeModule。
     * 不复用容器中的实例，避免全局序列化配置影响令牌字节；配置完成后是线程安全的。
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 签名密钥，来自配置文件 */
    @Value("${meeting.token.secret}")
    private String secret;

    /** 令牌有效期，单位秒 */
    @Value("${meeting.token.expire-seconds}")
    private long expireSeconds;

    /**
     * 签发 JWT。iat/exp 用秒级 NumericDate（RFC 7519），不是毫秒。
     */
    public String create(Long userId, String username, Integer role) {
        long now = Instant.now().getEpochSecond();

        ObjectNode header = MAPPER.createObjectNode();
        header.put("alg", JWT_ALG);
        header.put("typ", JWT_TYPE);

        ObjectNode payload = MAPPER.createObjectNode();
        // sub 必须是字符串（RFC 7519 定义为 StringOrURI），直接放数字不合规
        payload.put("sub", String.valueOf(userId));
        // 以下两个是冗余字段，仅供前端展示与排查问题，服务端不用于任何鉴权判断
        payload.put("username", username);
        payload.put("role", role);
        payload.put("iat", now);
        payload.put("exp", now + expireSeconds);

        String headerPart = encode(toJson(header));
        String payloadPart = encode(toJson(payload));
        String signingInput = headerPart + SEPARATOR + payloadPart;
        return signingInput + SEPARATOR + encode(hmac(signingInput));
    }

    /**
     * 校验并解析 JWT。
     * 任何一步不通过都返回 null，不向外抛异常：调用方只需区分「有效 / 无效」，不必知道失败原因（也不给探测者提供可区分的信息）。
     *
     * @return 校验通过的登录用户；令牌被篡改、格式非法或已过期返回 null
     */
    public LoginUser parse(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        // Base64Url 字母表不含 '.'，可直接按 '.' 切三段；-1 保留末尾空段
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3 || parts[0].isEmpty() || parts[1].isEmpty() || parts[2].isEmpty()) {
            return null;
        }
        try {
            // 1) 先卡算法：只接受 HS256，拒绝 alg=none 与其它算法，防算法混淆攻击
            JsonNode header = MAPPER.readTree(decode(parts[0]));
            if (!JWT_ALG.equals(header.path("alg").asText())) {
                return null;
            }

            // 2) 签名原文是「header段 + '.' + payload段」这两个 ASCII 串，用恒定时间比较
            byte[] expected = hmac(parts[0] + SEPARATOR + parts[1]);
            if (!MessageDigest.isEqual(expected, decode(parts[2]))) {
                return null;
            }

            // 3) 验签通过后才信任载荷
            JsonNode payload = MAPPER.readTree(decode(parts[1]));
            JsonNode sub = payload.get("sub");
            JsonNode username = payload.get("username");
            JsonNode role = payload.get("role");
            JsonNode iat = payload.get("iat");
            JsonNode exp = payload.get("exp");
            if (sub == null || !sub.isTextual()
                    || username == null || !username.isTextual()
                    || role == null || !role.isIntegralNumber()
                    || iat == null || !iat.isIntegralNumber()
                    || exp == null || !exp.isIntegralNumber()) {
                return null;
            }
            // RFC 7519：当前时间必须早于 exp，所以用 >= 判失效。
            // 签发与校验在同一台机器、同一个时钟源，不设容忍窗口。
            if (Instant.now().getEpochSecond() >= exp.asLong()) {
                return null;
            }
            // realName 不进令牌，由拦截器回查数据库后补上
            return new LoginUser(Long.parseLong(sub.asText()), username.asText(), role.asInt());
        } catch (Exception e) {
            // Base64 非法、JSON 损坏、sub 不是数字、字段类型不符……一律视为无效令牌
            return null;
        }
    }

    /** 计算 HMAC-SHA256，输入是「header段 + '.' + payload段」拼出的 ASCII 串 */
    private byte[] hmac(String signingInput) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII));
        } catch (Exception e) {
            throw new IllegalStateException("令牌签名计算失败", e);
        }
    }

    /** Base64Url 编码 */
    private String encode(byte[] bytes) {
        return ENCODER.encodeToString(bytes);
    }

    /** Base64Url 解码 */
    private byte[] decode(String part) {
        return DECODER.decode(part);
    }

    /** 把 JSON 节点序列化成字节，供编码与签名使用 */
    private byte[] toJson(JsonNode node) {
        try {
            return MAPPER.writeValueAsBytes(node);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("令牌 JSON 序列化失败", e);
        }
    }
}
