package com.supercode.framework.ctypto.signature.rsa.pool;

import com.supercode.framework.ctypto.signature.rsa.pool.factory.PooledSignatureVerifierFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import com.supercode.framework.ctypto.signature.ISignatureVerifier;
import com.supercode.framework.ctypto.signature.rsa.RSASignatureVerifier;

/**
 * @author jonathan.ji
 */
@Slf4j
public class PooledRSASignatureVerifier implements ISignatureVerifier {

    private final GenericObjectPool<RSASignatureVerifier> pool;

    public PooledRSASignatureVerifier(String publicKeyStr) {
        this(publicKeyStr, 20, 10);
    }

    public PooledRSASignatureVerifier(String publicKeyStr, int maxTotal, int maxIdle) {
        GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
        conf.setMaxTotal(maxTotal > 0 ? maxTotal : 20);
        conf.setMaxIdle(maxIdle < maxTotal ? maxIdle : 10);
        pool = new GenericObjectPool<>(new PooledSignatureVerifierFactory(publicKeyStr), conf);
    }

    @Override
    public boolean verifySignature(byte[] body, String signature) {
        RSASignatureVerifier verifier = null;
        try {
            verifier = pool.borrowObject();
            return verifier.verifySignature(body, signature);
        } catch (Exception e) {
            log.error("cannot borrow RSASignatureVerifier from pool", e);
            return false;
        } finally {
            if (verifier != null) {
                pool.returnObject(verifier);
            }
        }
    }
}
