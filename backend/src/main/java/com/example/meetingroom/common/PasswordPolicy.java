package com.example.meetingroom.common;

/**
 * 密码策略。
 * <p>
 * 长度上下限被 {@code @Size} 注解直接引用，注解参数必须是编译期常量，
 * 因此这里用 static final 常量而不是配置项——它是注册与改密的硬性约束，
 * 不适合做成可在线调整的参数（管理员把下限改成 1 就等于取消密码强度要求）。
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 6;

    public static final int MAX_LENGTH = 32;

    private PasswordPolicy() {
    }
}
