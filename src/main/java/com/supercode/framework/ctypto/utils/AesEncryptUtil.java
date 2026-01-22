package com.supercode.framework.ctypto.utils;

import com.supercode.framework.ctypto.utils.common.EncryptionCustomizeException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class AesEncryptUtil {

    private static final byte[] IV = new IvParameterSpec(new byte[16]).getIV();

    public static String encrypt(String content, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, IV);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(content.getBytes()));
        } catch (Exception e) {
            throw new EncryptionCustomizeException("encryption error", e);
        }
    }

    public static String decrypt(String content, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, IV);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(content)));
        } catch (Exception e) {
            throw new EncryptionCustomizeException("encryption error", e);
        }
    }

    public static byte[] generateKey() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[16]; // 128-bit key
        secureRandom.nextBytes(key);
        return key;
    }

}
