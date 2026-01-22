package com.supercode.framework.utils;

import com.supercode.master.utils.number.RandomUtil;

/**
 * @author jonathan.ji
 */
public class VerificationCodeUtil {

    /**
     * create 6-digit verification code
     */
    public static String createSixDigitNormalCode() {
        return createNormalCode(100000, 999999);
    }

    public static String createNormalCode(int minLength, int maxLength) {
        return String.valueOf(RandomUtil.nextLong(minLength, maxLength));
    }
}
