package com.jinphy.simplechatserver.utils;

import java.util.HashMap;
import java.util.Map;

public class StringUtils {


    /**
     * DESC: 将网络请求的url路劲中的参数部分解析成Map
     * Created by jinphy, on 2017/12/5, at 20:23
     */
    public static Map<String, String> toMap(String params) {
        String[] split = params.split("&");
        Map<String, String> map = new HashMap<>(split.length);
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
}
