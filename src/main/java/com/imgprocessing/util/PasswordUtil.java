package com.imgprocessing.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {

    // Sử dụng thuật toán SHA-256
    private static final String HASH_ALGORITHM = "SHA-256";

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            // "digest" thực hiện băm, trả về một mảng byte[]
            byte[] hashedBytes = md.digest(password.getBytes());

            // Chuyển mảng byte[] thành chuỗi Hex (dễ lưu vào CSDL)
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi nghiêm trọng: Không tìm thấy thuật toán băm", e);
        }
    }

    public static boolean checkPassword(String rawPassword, String storedHash) {
        String newHash = hashPassword(rawPassword);
        return newHash.equals(storedHash);
    }
}