package com.supercode.framework.ctypto.utils.asymmetric;

import com.supercode.framework.ctypto.utils.common.EncryptionCustomizeException;

/**
 * @author jonathan.ji
 */
public interface IEncryptor {

    byte[] encrypt(byte[] data) throws EncryptionCustomizeException;
}
