package com.huashan.smartmallbackend.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 密码加密工具类
 * <p>
 * 采用「MD5 摘要 + 固定盐值」的方式对密码加密，基于 JDK 内置 MessageDigest，
 * 不依赖任何第三方加密库。数据库存储加密后的密文，登录时对明文加密后比对。
 * <p>
 * 预置账号密文（可用本类 main 方法复算）：
 * <ul>
 *     <li>admin123 -&gt; 7eca1b0a854dd5dc29995c68a9a4bb02</li>
 *     <li>123456   -&gt; 61bd06a2dc8d29ab50f2b8726f65d4fb</li>
 * </ul>
 *
 * @author hs
 */
public final class MD5Util {

    /**
     * 盐值（拼接在密码尾部，防止简单彩虹表碰撞）
     */
    private static final String SALT = "mini_mall_2026";

    /**
     * 十六进制字符表
     */
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    /**
     * 私有构造，禁止实例化工具类
     */
    private MD5Util() {
    }

    /**
     * 对明文密码加密（MD5 + 盐）
     *
     * @param rawPassword 明文密码
     * @return 32 位小写十六进制密文
     */
    public static String encrypt(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("密码不能为空");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            // 密码拼接盐值后计算摘要
            byte[] bytes = digest.digest((rawPassword + SALT).getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            // JDK 必然内置 MD5，正常不会走到这里
            throw new IllegalStateException("MD5 算法不可用", e);
        }
    }

    /**
     * 校验明文密码与密文是否匹配
     *
     * @param rawPassword       明文密码
     * @param encryptedPassword 数据库中存储的密文
     * @return true 匹配，false 不匹配
     */
    public static boolean verify(String rawPassword, String encryptedPassword) {
        if (rawPassword == null || encryptedPassword == null) {
            return false;
        }
        return encrypt(rawPassword).equals(encryptedPassword);
    }

    /**
     * 字节数组转小写十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String toHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            chars[i * 2] = HEX_CHARS[b >>> 4];
            chars[i * 2 + 1] = HEX_CHARS[b & 0x0F];
        }
        return new String(chars);
    }

    /**
     * 测试方法：打印预置账号密文，便于核对 init.sql
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        System.out.println("admin123 -> " + encrypt("admin123"));
        System.out.println("123456   -> " + encrypt("123456"));
        System.out.println("校验 admin123: " + verify("admin123", "7eca1b0a854dd5dc29995c68a9a4bb02"));
        System.out.println("校验 123456  : " + verify("123456", "61bd06a2dc8d29ab50f2b8726f65d4fb"));
    }
}