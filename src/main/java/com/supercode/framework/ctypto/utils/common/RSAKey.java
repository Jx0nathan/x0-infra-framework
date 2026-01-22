package com.supercode.framework.ctypto.utils.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Data
@AllArgsConstructor
public class RSAKey {
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;
}
