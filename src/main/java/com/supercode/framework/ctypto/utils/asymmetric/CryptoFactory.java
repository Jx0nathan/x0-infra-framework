package com.supercode.framework.ctypto.utils.asymmetric;

import com.supercode.framework.ctypto.utils.asymmetric.encryption.DecryptorImpl;
import com.supercode.framework.ctypto.utils.asymmetric.encryption.EncryptorImpl;

/**
 * @author jonathan.ji
 */
public class CryptoFactory {

    /**
     * @param publicKey
     * @return
     */
    public static IEncryptor createEncryptor(String publicKey) {
        return new EncryptorImpl(publicKey);
    }

    /**
     * @param privateKey
     * @return
     */
    public static IDecryptor createDecryptor(String privateKey) {
        return new DecryptorImpl(privateKey);
    }
}
