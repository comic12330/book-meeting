package com.example.meetingroom.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 密码摘要工具。
 * 说明：任务书允许 BCrypt 或 MD5，本项目为保证数据库脚本可离线执行选用 MD5。
 * 如需升级为 BCrypt，只需替换本类的 encode/verify 实现，调用方无需改动。
 */
public final class Md5Util {

    private Md5Util() {
    }

    public static String encode(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xFF));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 算法不可用", e);
        }
    }

    public static boolean verify(String raw, String stored) {
        return stored != null && stored.equalsIgnoreCase(encode(raw));
    }
}
