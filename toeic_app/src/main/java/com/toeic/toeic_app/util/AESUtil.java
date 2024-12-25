package com.toeic.toeic_app.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AESUtil {
    private static final String AES = "AES";

    // Tạo khóa từ chuỗi
    public static SecretKey generateKeyFromString(String keyString) {
        byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
        byte[] paddedKeyBytes = new byte[16]; // AES cần 16 byte (128 bit)
        System.arraycopy(keyBytes, 0, paddedKeyBytes, 0, Math.min(keyBytes.length, paddedKeyBytes.length));
        return new SecretKeySpec(paddedKeyBytes, AES);
    }

    // Mã hóa dữ liệu
    public static String encrypt(String data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Giải mã dữ liệu
    public static String decrypt(String encryptedData, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] originalBytes = cipher.doFinal(decodedBytes);
        return new String(originalBytes);
    }
}
