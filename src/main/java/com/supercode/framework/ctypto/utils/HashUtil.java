package com.supercode.framework.ctypto.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import com.supercode.framework.ctypto.utils.common.EncryptionCustomizeException;

import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static com.supercode.framework.ctypto.utils.common.CryptoConstants.*;

/**
 * 单向散列函数 SHA256
 *
 * @author jonathan.ji
 */
public class HashUtil {

    private final static String KEY_SHA = "SHA";

    /**
     * PBKDF2（Password-Based Key Derivation Function 2）算法对内容进行哈希的方法
     * ITERATIONS ： 迭代次数
     * SECRET_KEY_BYTES * 8 ： 密钥长度位数
     */
    public static byte[] hashWithPBKDF2(String content, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(content.toCharArray(), salt, ITERATIONS, SECRET_KEY_BYTES * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF_2_WITH_HMAC_SHA_256);
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new EncryptionCustomizeException("hash exception occur for algorithm PBKDF2WithHmacSHA256", e);
        }
    }

    public static byte[] hashWithPBKDF2(String content, boolean randomSalt) {
        byte[] salt;
        if (randomSalt) {
            salt = genRandomSalt();
        } else {
            salt = new byte[SECRET_KEY_BYTES];
        }
        return hashWithPBKDF2(content, salt);
    }

    private static byte[] genRandomSalt() {
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            byte[] salt = new byte[SECRET_KEY_BYTES];
            random.nextBytes(salt);
            return salt;
        } catch (Exception e) {
            throw new EncryptionCustomizeException("no SHA1PRNG algorithm was found", e);
        }
    }

    public static String sha256Hex(String data) {
        return DigestUtils.sha256Hex(data);
    }


    /**
     * HmacSHA256 加密， 返回结果以base64处理
     */
    public static String hmacSha256(String data, String secret, Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("charset cannot be null");
        }
        try {
            Mac sha256hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(charset), "HmacSHA256");
            sha256hmac.init(secretKey);
            return Base64.encodeBase64String(sha256hmac.doFinal(data.getBytes(charset)));
        } catch (Exception e) {
            throw new RuntimeException("hmacSha256 exception, data:" + data, e);
        }
    }

    /**
     * 采用HmacSHA256算法对数据进行hash.
     *
     * @param data   待哈希的数据，默认使用 ISO_8859_1编码格式
     * @param secret 哈希算法使用的密钥，默认使用 ISO_8859_1编码格式
     */
    public static String hmacSha256(String data, String secret) {
        return hmacSha256(data, secret, StandardCharsets.ISO_8859_1);
    }
}
