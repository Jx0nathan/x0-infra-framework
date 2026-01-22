package com.supercode.framework.ctypto.utils;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * @author jonathan.ji
 */
public class Base64Util {

    private static final Pattern PTN_BASE64 =
            Pattern.compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$");

    /**
     * base64编码
     */
    public static String encode(String data) {
        return encode(data, StandardCharsets.UTF_8);
    }

    /**
     * base64编码
     */
    public static String encode(String data, Charset charset) {
        return Base64.encodeBase64String(data.getBytes(charset));
    }

    /**
     * base64解码
     * decode the specified data that with UTF-8 encoding.
     */
    public static String decode(String data) {
        return decode(data, StandardCharsets.UTF_8);
    }

    public static String decode(String data, Charset charset) {
        return new String(decodeBytes(data.getBytes(charset)), charset);
    }

    /**
     * base64解码
     */
    public static byte[] decodeBytes(byte[] rawData) {
        return Base64.decodeBase64(rawData);
    }

    /**
     * 判断字符串是否经过base64编码
     *
     * @param str
     * @return true or false
     */
    public static boolean isBase64(String str) {
        if (str == null) {
            return false;
        }
        return PTN_BASE64.matcher(str).matches();
    }
}
