package com.supercode.framework.util;

import com.supercode.framework.ctypto.utils.AesEncryptUtil;
import org.junit.jupiter.api.Test;

import java.util.Base64;

/**
 * @Author: Aaron Wu
 * @Date: 2023/10/18 21:14
 */
public class AesEncryptUtilTest {

    @Test
    void test() {
        String plaintext = "Hello, World!";
        String pw = Base64.getEncoder().encodeToString(AesEncryptUtil.generateKey());
        System.out.println(pw);
        String ciphertext = AesEncryptUtil.encrypt(plaintext, pw);
        System.out.println("AES/GCM ciphertext: " + ciphertext);
        String decryptedText = AesEncryptUtil.decrypt(ciphertext, pw);
        System.out.println("AES/GCM decrypted text: " + decryptedText);
    }

}
