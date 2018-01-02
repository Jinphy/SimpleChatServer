package com.jinphy.simplechatserver.utils;

import java.util.HashMap;
import java.util.Map;

public class StringUtils {


    /**
     * DESC: 将网络请求的url路劲中的参数部分解析成Map
     * Created by jinphy, on 2017/12/5, at 20:23
     */
    public static Map<String, String> toMap(String params) {
        if (StringUtils.isEmpty(params)) {
            return new HashMap<>(5);
        }
        String[] split = params.split("&");
        Map<String, String> map = new HashMap<>(split.length+5);
        for (String s : split) {
            String[] item = s.split("=");
            map.put(item[0], item[1]);
        }
        return map;
    }

    /**
     * DESC: 判断一个字符串是否为null或者是空串
     * Created by jinphy, on 2017/12/5, at 20:42
     */
    public static boolean isEmpty(String text) {
        return text == null || text.trim().length() == 0;
    }

    /**
     * DESC: 判断一个字符串是否不为null和空串
     * Created by jinphy, on 2017/12/5, at 20:44
     */
    public static boolean isNonEmpty(String text) {
        return !isEmpty(text);
    }




    /**
     * DESC: 将字节数组转换成16进制字符串
     * Created by jinphy, on 2017/12/31, at 20:15
     */
    public static String bytes2HexString(byte[] b) {
        StringBuffer result = new StringBuffer();
        String hex;
        for (int i = 0; i < b.length; i++) {
            hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            result.append(hex.toUpperCase());
        }
        return result.toString();
    }

    /**
     * DESC: 将16进制的字符串转换成字节数组
     * Created by jinphy, on 2017/12/31, at 20:21
     */
    public static byte[] hexString2Bytes(String src) {
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            ret[i] = (byte) Integer.valueOf(src.substring(i * 2, i * 2 + 2), 16).byteValue();
        }
        return ret;
    }
}
