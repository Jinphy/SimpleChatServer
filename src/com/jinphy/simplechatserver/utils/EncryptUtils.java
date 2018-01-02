package com.jinphy.simplechatserver.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.jinphy.simplechatserver.constants.StringConst.MD5;
import static com.jinphy.simplechatserver.constants.StringConst.UTF_8;

/**
 * Created by jinphy on 2017/8/9.
 */

public class EncryptUtils {

    // AES 加密秘钥
    public static final String AES_KEY = "OhgkU9HlPbmmXvFpZd2zStk8HfVNHMd4cAbtuNwrpeyUyCMyNFuDHXgiAYBKgcQZNJUatazKWp7eiE4mdmqccQ9ourHF6Hz0WrjrXnbdQxDQ3JCC0i7kgXH6wwWLdSv0";


    private EncryptUtils() {
    }


    /**
     * 将一段文本信息按指定次数进行MD5加密
     *
     * @param msg   待加密的文本信息
     * @param times 加密次数
     */
    public static String md5(String msg, int times) {
        if (times < 1) {
            times = 1;
        }
        for (int i = 0; i < times; i++) {
            msg = md5(msg);
        }
        return msg;
    }

    /**
     * 将一段文本信息进行MD5加密，加密一个
     *
     * @param msg 待加密的文本信息
     */
    public static String md5(String msg) {
        try {
            MessageDigest digest = MessageDigest.getInstance(MD5);
            byte[] encryptedMsg = digest.digest(msg.getBytes(UTF_8));

            return parseBytes(encryptedMsg);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * DESC: AES 加密
     * Created by jinphy, on 2017/12/31, at 20:27
     */
    public static String aesEncrypt(String value, String... aesKey) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        String key = AES_KEY;
        if (aesKey.length > 0) {
            key = aesKey[0];
        }
        String s = aesTransform(value, key);
        try {
            return StringUtils.bytes2HexString(s.getBytes(UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }

    }

    /**
     * DESC: AES 解密
     * Created by jinphy, on 2017/12/31, at 20:29
     */
    public static String aesDecrypt(String value, String... aesKey) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        try {
            String s = new String(StringUtils.hexString2Bytes(value), UTF_8);

            String key = AES_KEY;
            if (aesKey.length > 0) {
                key = aesKey[0];
            }
            return EncryptUtils.aesTransform(s, key);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * DESC: AES 转换
     * Created by jinphy, on 2017/12/31, at 20:12
     */
    public static String aesTransform(String value, String aesKey) {
        int[] iS = new int[256];
        byte[] iK = new byte[256];

        for (int i = 0; i < 256; i++)
            iS[i] = i;

        int j = 1;

        for (short i = 0; i < 256; i++) {
            iK[i] = (byte) aesKey.charAt((i % aesKey.length()));
        }

        j = 0;

        for (int i = 0; i < 255; i++) {
            j = (j + iS[i] + iK[i]) % 256;
            int temp = iS[i];
            iS[i] = iS[j];
            iS[j] = temp;
        }

        int i = 0;
        j = 0;
        char[] iInputChar = value.toCharArray();
        char[] iOutputChar = new char[iInputChar.length];
        for (int x = 0; x < iInputChar.length; x++) {
            i = (i + 1) % 256;
            j = (j + iS[i]) % 256;
            int temp = iS[i];
            iS[i] = iS[j];
            iS[j] = temp;
            int t = (iS[i] + (iS[j] % 256)) % 256;
            int iY = iS[t];
            char iCY = (char) iY;
            iOutputChar[x] = (char) (iInputChar[x] ^ iCY);
        }

        return new String(iOutputChar);
    }










    //    将用md5加密后的字节数组进行解析
    private static String parseBytes(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            builder.append(parseByte(aByte));
        }
        return builder.toString().toUpperCase();
    }

    //    解析每个字节
    private static String parseByte(byte b) {
        String temp = Integer.toHexString(b & 0xff);
        return temp.length() == 1 ? 0 + temp : temp;
    }
}