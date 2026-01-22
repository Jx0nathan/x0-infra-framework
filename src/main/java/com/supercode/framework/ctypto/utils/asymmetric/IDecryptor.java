package com.supercode.framework.ctypto.utils.asymmetric;

import org.bouncycastle.openssl.EncryptionException;
import com.supercode.framework.ctypto.utils.common.EncryptionCustomizeException;

/**
 * @author jonatham.ji
 */
public interface IDecryptor {

    /**
     * decrypt the data.
     *
     * @param encryptedData
     * @return
     * @throws EncryptionException
     */
    byte[] decrypt(byte[] encryptedData) throws EncryptionCustomizeException;
}
