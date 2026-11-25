package com.example.meetingroom.unit;

import com.example.meetingroom.common.LoginUser;
import com.example.meetingroom.util.TokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 令牌签发与校验的单元测试。
 *
 * 不启动 Spring 容器：{@link TokenUtil} 的两个配置项用反射直接注入，这样测试只验证算法与校验逻辑本身，不受数据源、Web 环境的影响。
 * 覆盖的是「令牌被伪造会怎样」，而不是「正常签发能用」，后者几乎不可能写错，前者才是这套代码存在的意义。其中 alg=none 一条必须用真密钥重算签名，否则无法区分「被算法校验拦住」和「验签恰好失败」。
 */
class TokenUtilTest {

    /** 与 application.yml 中的形态一致：足够 32 字节，满足 HS256 的密钥长度要求 */
    private static final String SECRET = "unit-test-hmac-secret-0123456789abcdef";

    private static final long EXPIRE_SECONDS = 7200L;

    private TokenUtil tokenUtil;

    @BeforeEach
    void setUp() {
        tokenUtil = new TokenUtil();
        ReflectionTestUtils.setField(tokenUtil, "secret", SECRET);
        ReflectionTestUtils.setField(tokenUtil, "expireSeconds", EXPIRE_SECONDS);
    }

    @Test
    @DisplayName("签发后可解析回原始用户信息")
    void createAndParse() {
        String token = tokenUtil.create(7L, "zhangsan", 0);

        LoginUser user = tokenUtil.parse(token);

        assertNotNull(user, "刚签发的令牌必须能通过校验");
        assertEquals(7L, user.getUserId());
        assertEquals("zhangsan", user.getUsername());
        assertEquals(0, user.getRole());
        // realName 不进令牌载荷，由拦截器回查数据库后补上
        assertNull(user.getRealName());
    }

    @Test
    @DisplayName("令牌为三段式且不含 Base64 补位的等号")
    void tokenHasThreeSegments() {
        String token = tokenUtil.create(1L, "admin", 1);

        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
        for (String part : parts) {
            assertTrue(part.length() > 0);
            assertTrue(!part.contains("="), "RFC 7515 要求 Base64Url 去掉补位等号");
        }
    }

    @Test
    @DisplayName("sub 是字符串，exp 与 iat 为秒级且差值为配置的有效期")
    void payloadFollowsRfc() {
        String token = tokenUtil.create(1L, "admin", 1);
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]),
                StandardCharsets.UTF_8);

        assertTrue(payload.contains("\"sub\":\"1\""), "sub 必须是字符串而不是数字，实际载荷：" + payload);
        assertTrue(payload.matches(".*\"iat\":\\d{10}.*"), "iat 应为秒级 NumericDate，实际载荷：" + payload);
        assertTrue(payload.matches(".*\"exp\":\\d{10}.*"), "exp 应为秒级 NumericDate，实际载荷：" + payload);

        long iat = Long.parseLong(payload.replaceAll(".*\"iat\":(\\d+).*", "$1"));
        long exp = Long.parseLong(payload.replaceAll(".*\"exp\":(\\d+).*", "$1"));
        assertEquals(EXPIRE_SECONDS, exp - iat);
    }

    @Test
    @DisplayName("篡改载荷后校验失败")
    void tamperedPayloadRejected() {
        String token = tokenUtil.create(2L, "zhangsan", 0);
        String[] parts = token.split("\\.");

        // 把用户 ID 改成管理员，签名不变
        String forgedPayload = new String(Base64.getUrlDecoder().decode(parts[1]),
                StandardCharsets.UTF_8).replace("\"sub\":\"2\"", "\"sub\":\"1\"");
        String forged = parts[0] + "." + base64Url(forgedPayload.getBytes(StandardCharsets.UTF_8))
                + "." + parts[2];

        assertNull(tokenUtil.parse(forged));
    }

    @Test
    @DisplayName("篡改签名段后校验失败")
    void tamperedSignatureRejected() {
        String token = tokenUtil.create(2L, "zhangsan", 0);
        String[] parts = token.split("\\.");

        // 改签名段的首个字符，而不是末位：32 字节摘要的 Base64Url 编码有 43 个字符，末位字符只含 4 个有效位、低 2 位是补位噪声，改它可能解码出完全相同的字节，测试就变成「没篡改也通过」的假阴性。
        char first = parts[2].charAt(0);
        char flipped = first == 'A' ? 'B' : 'A';
        String forged = parts[0] + "." + parts[1] + "." + flipped + parts[2].substring(1);

        assertNull(tokenUtil.parse(forged));
    }

    @Test
    @DisplayName("alg=none 即使签名用真密钥重算也被拒绝")
    void algNoneRejected() {
        // 算法混淆攻击：把 header 改成 none，再用真实密钥对前两段算一次签名。
        // 如果实现只验签不校验 alg，这个令牌就会被当成合法令牌放行。
        String header = base64Url("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        long now = System.currentTimeMillis() / 1000;
        String payload = base64Url(("{\"sub\":\"1\",\"username\":\"admin\",\"role\":1,\"iat\":"
                + now + ",\"exp\":" + (now + 3600) + "}").getBytes(StandardCharsets.UTF_8));
        String forged = header + "." + payload + "." + sign(header + "." + payload);

        assertNull(tokenUtil.parse(forged), "算法必须是 HS256，none 一律拒绝");
    }

    @Test
    @DisplayName("已过期的令牌校验失败")
    void expiredTokenRejected() {
        // 有效期设为 0：exp 等于当前秒，按 RFC「当前时间必须早于 exp」判为失效
        ReflectionTestUtils.setField(tokenUtil, "expireSeconds", 0L);
        String token = tokenUtil.create(1L, "admin", 1);

        assertNull(tokenUtil.parse(token));
    }

    @Test
    @DisplayName("旧的两段式令牌不再被接受")
    void legacyTwoSegmentTokenRejected() {
        String legacy = base64Url(("1|admin|1|" + (System.currentTimeMillis() + 7200_000))
                .getBytes(StandardCharsets.UTF_8)) + "." + "0123456789abcdef";

        assertNull(tokenUtil.parse(legacy));
    }

    @Test
    @DisplayName("空串、null、段数不对、含空段一律无效")
    void malformedTokenRejected() {
        assertNull(tokenUtil.parse(null));
        assertNull(tokenUtil.parse(""));
        assertNull(tokenUtil.parse("only-one-part"));
        assertNull(tokenUtil.parse("a.b"));
        assertNull(tokenUtil.parse("a.b.c.d"));
        assertNull(tokenUtil.parse(".."));
        assertNull(tokenUtil.parse("a..c"));
        // 合法结构但内容不是 Base64/JSON
        assertNull(tokenUtil.parse("aaa.bbb.ccc"));
    }

    @Test
    @DisplayName("中文用户名往返不乱码")
    void chineseUsernameRoundTrip() {
        String token = tokenUtil.create(9L, "张三", 0);

        LoginUser user = tokenUtil.parse(token);

        assertNotNull(user);
        assertEquals("张三", user.getUsername(),
                "签名原文按 US_ASCII 处理、JSON 按 UTF-8 解码，两处编码写错就会乱码");
    }

    // ------------------------------------------------------------ 辅助方法

    /** Base64Url 编码，不带补位符，与令牌实现保持一致 */
    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** 用真实密钥重算签名，供 alg=none 用例构造「签名正确但算法非法」的令牌 */
    private static String sign(String signingInput) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return base64Url(mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
