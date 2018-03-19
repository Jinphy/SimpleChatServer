package com.jinphy.simplechatserver.utils;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Created by jinphy on 2017/12/5.
 */
public class GsonUtils {

    static Gson gson = new Gson();

    /**
     * DESC: 把json转换成JavaBean
     * Created by jinphy, on 2017/12/4, at 22:52
     */
    public static <T> T toBean(String str, Class<T> clazz) {
        return gson.fromJson(str, clazz);
    }


    /**
     * DESC: 把json转换成JavaBean
     * Created by jinphy, on 2017/12/4, at 22:52
     */
    public static <T> T toBean(String str, Type type) {
        return gson.fromJson(str, type);
    }

    /**
     * DESC: 把JavaBean转换成json
     * Created by jinphy, on 2017/12/4, at 22:55
     */
    public static <T> String toJson(T bean) {
        return gson.toJson(bean);
    }
}
