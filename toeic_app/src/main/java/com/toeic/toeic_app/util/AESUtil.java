package com.toeic.toeic_app.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AESUtil {
    private static final String AES = "AES";

//    // Tạo khóa từ chuỗi
//    public static SecretKey generateKeyFromString(String keyString) {
//        byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
//        byte[] paddedKeyBytes = new byte[16]; // AES cần 16 byte (128 bit)
//        System.arraycopy(keyBytes, 0, paddedKeyBytes, 0, Math.min(keyBytes.length, paddedKeyBytes.length));
//        return new SecretKeySpec(paddedKeyBytes, AES);
//    }
//
//    // Mã hóa dữ liệu
//    public static String encrypt(String data, SecretKey secretKey) throws Exception {
//        Cipher cipher = Cipher.getInstance(AES);
//        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
//        return Base64.getEncoder().encodeToString(encryptedBytes);
//    }
//
//    // Giải mã dữ liệu
//    public static String decrypt(String encryptedData, SecretKey secretKey) throws Exception {
//        Cipher cipher = Cipher.getInstance(AES);
//        cipher.init(Cipher.DECRYPT_MODE, secretKey);
//        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
//        byte[] originalBytes = cipher.doFinal(decodedBytes);
//        return new String(originalBytes);
//    }

    public static final String AES_CBC_PADDING = "AES/CBC/PKCS5Padding";

    // Tạo khóa từ chuỗi
    public static SecretKey generateKeyFromString(String keyString) {
        byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
        byte[] paddedKeyBytes = new byte[16]; // AES cần 16 byte (128 bit)
        System.arraycopy(keyBytes, 0, paddedKeyBytes, 0, Math.min(keyBytes.length, paddedKeyBytes.length));
        return new SecretKeySpec(paddedKeyBytes, AES);
    }

    // Tạo IV ngẫu nhiên
    public static IvParameterSpec generateIV() {
        byte[] iv = new byte[16]; // IV phải có kích thước 16 byte
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    // Mã hóa dữ liệu
    public static String encrypt(String data, SecretKey secretKey, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CBC_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Kết hợp IV và dữ liệu mã hóa thành một chuỗi
        byte[] combined = new byte[iv.getIV().length + encryptedBytes.length];
        System.arraycopy(iv.getIV(), 0, combined, 0, iv.getIV().length);
        System.arraycopy(encryptedBytes, 0, combined, iv.getIV().length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    // Giải mã dữ liệu
//    public static String decrypt(String encryptedData, SecretKey secretKey) throws Exception {
//        byte[] combined = Base64.getDecoder().decode(encryptedData);
//
//        // Tách IV và dữ liệu mã hóa
//        byte[] iv = new byte[16];
//        byte[] encryptedBytes = new byte[combined.length - iv.length];
//        System.arraycopy(combined, 0, iv, 0, iv.length);
//        System.arraycopy(combined, iv.length, encryptedBytes, 0, encryptedBytes.length);
//
//        IvParameterSpec ivSpec = new IvParameterSpec(iv);
//
//        Cipher cipher = Cipher.getInstance(AES_CBC_PADDING);
//        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
//        byte[] originalBytes = cipher.doFinal(encryptedBytes);
//        return new String(originalBytes, StandardCharsets.UTF_8);
//    }

    public static String decrypt(String encryptedData, SecretKey secretKey) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedData);

        // Tách IV và dữ liệu mã hóa
        byte[] iv = new byte[16];  // IV có độ dài 16 byte
        byte[] encryptedBytes = new byte[combined.length - iv.length];

        System.arraycopy(combined, 0, iv, 0, iv.length); // Lấy IV từ đầu dữ liệu mã hóa
        System.arraycopy(combined, iv.length, encryptedBytes, 0, encryptedBytes.length); // Lấy phần dữ liệu mã hóa

        IvParameterSpec ivSpec = new IvParameterSpec(iv); // Khởi tạo IvParameterSpec từ IV

        Cipher cipher = Cipher.getInstance(AES_CBC_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec); // Sử dụng IV trong quá trình giải mã

        byte[] originalBytes = cipher.doFinal(encryptedBytes);
        return new String(originalBytes, StandardCharsets.UTF_8);
    }

}
