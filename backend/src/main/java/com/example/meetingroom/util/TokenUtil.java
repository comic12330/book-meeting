package com.example.meetingroom.util;

import com.example.meetingroom.common.LoginUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 令牌工具：载荷 + HmacSHA256 签名，无状态、不依赖 Session。
 * 载荷格式：userId|username|role|expireAt
 */
@Component
public class TokenUtil {

    private static final String ALGORITHM = "HmacSHA256";

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    @Value("${meeting.token.secret}")
    private String secret;

    @Value("${meeting.token.expire-seconds}")
    private long expireSeconds;

    public String create(Long userId, String username, Integer role) {
        long expireAt = System.currentTimeMillis() + expireSeconds * 1000L;
        String payload = userId + "|" + username + "|" + role + "|" + expireAt;
        String body = ENCODER.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return body + "." + sign(body);
    }

    /**
     * @return 校验通过的登录用户；签名不符、格式错误或已过期返回 null
     */
    public LoginUser parse(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        int dot = token.indexOf('.');
        if (dot <= 0 || dot == token.length() - 1) {
            return null;
        }
        String body = token.substring(0, dot);
        String signature = token.substring(dot + 1);
        if (!sign(body).equals(signature)) {
            return null;
        }
        try {
            String payload = new String(DECODER.decode(body), StandardCharsets.UTF_8);
            String[] parts = payload.split("\\|");
            if (parts.length != 4) {
                return null;
            }
            long expireAt = Long.parseLong(parts[3]);
            if (System.currentTimeMillis() > expireAt) {
                return null;
            }
            return new LoginUser(Long.parseLong(parts[0]), parts[1], Integer.parseInt(parts[2]));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }

    private String sign(String body) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] raw = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) {
                sb.append(String.format("%02x", b & 0xFF));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("令牌签名失败", e);
        }
    }
}
