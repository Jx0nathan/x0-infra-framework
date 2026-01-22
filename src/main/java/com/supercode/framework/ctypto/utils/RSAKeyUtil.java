package com.supercode.framework.ctypto.utils;

import com.supercode.framework.ctypto.utils.common.RSAKey;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static com.supercode.framework.ctypto.utils.common.CryptoConstants.KEY_RSA;

/**
 * @author jonathan.ji
 */
public class RSAKeyUtil {

    /**
     * 生成密钥对(公钥和私钥), 密钥长度为2048位.
     *
     * @return
     * @throws Exception
     */
    public static RSAKey genKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_RSA);
        keyPairGen.initialize(2048);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey(publicKey, privateKey);
        return rsaKey;
    }
}
