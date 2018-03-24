package com.jinphy.simplechatserver.utils;

import org.java_websocket.util.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.jinphy.simplechatserver.constants.StringConst.UTF_8;

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
     * DESC: 判断是否是空
     * Created by jinphy, on 2017/12/5, at 20:42
     */
    public static boolean isEmpty(Object... values) {
        for (Object value : values) {
            if (value == null || value.toString().length() == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * DESC: 判断是否是空
     * Created by jinphy, on 2018/1/3, at 14:58
     */
    public static boolean isTrimEmpty(Object... values) {
        for (Object value : values) {
            if (value == null || value.toString().trim().length() == 0) {
                return true;
            }
        }
        return false;
    }



    /**
     * DESC: 判断一个字符串是否不为null和空串
     * Created by jinphy, on 2017/12/5, at 20:44
     */
    public static boolean isNonEmpty(String text) {
        return !isEmpty(text);
    }


    /**
     * DESC: 把对象转换成String后再用指定的字符首尾包装
     *
     * @param wrapper 用来包装的字符串，默认最多只接受两个字符串，多的将被忽略，
     *                如果只有一个则首尾都用该字符串，如果不传则返回原字符串，
     *                如果为空则返回空
     * Created by jinphy, on 2018/1/4, at 0:18
     */
    public static String wrap(Object value, String... wrapper) {
        if (value == null || wrapper.length == 0) {
            return null;
        }
        String first = wrapper[0];
        String last;
        if (wrapper.length > 1) {
            last = wrapper[1];
        } else {
            last = first;
        }
        return first + value + last;
    }


    /**
     * DESC: 判断两个字符串是否相等
     * Created by jinphy, on 2018/1/4, at 14:55
     */
    public static boolean equal(String first, String second) {
        if (first == null) {
            return false;
        }
        return first.equals(second);
    }

    /**
     * DESC: 判断两个字符串是否不相等
     * Created by jinphy, on 2018/1/4, at 14:56
     */
    public static boolean noEqual(String first, String second) {
        return !equal(first, second);
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



    public static String bytesToStr(byte[] source) {
        if (source == null) {
            return "";
        }
        return Base64.encodeBytes(source);
    }


    public static String bytesToStr(byte[] source, int offset, int len) {
        if (source == null) {
            return "";
        }
        return org.java_websocket.util.Base64.encodeBytes(source, offset, len);
    }

    public static byte[] strToBytes(String source) {
        try {
            return Base64.decode(source);
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }



}
