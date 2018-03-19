package com.jinphy.simplechatserver.utils;

import com.jinphy.simplechatserver.MyNullPointerException;

/**
 * Created by jinphy on 2017/12/5.
 */
public class ObjectHelper {


    /**
     * DESC: 检测非空
     * Created by jinphy, on 2017/12/5, at 22:48
     */
    public static void requireMyNonNull(Object object, String msg) throws MyNullPointerException {
        if (object == null) {
            throw new MyNullPointerException(msg);
        }
    }


    /**
     * DESC: 检测非空
     * Created by jinphy, on 2017/12/5, at 22:48
     */
    public static void requireNonNull(Object object, String msg)  {
        if (object == null) {
            throw new NullPointerException(msg);
        }
    }




    /**
     * DESC: 抛出空指针异常
     * Created by jinphy, on 2018/1/3, at 19:56
     */
    public static void throwNull(String msg) {
        throw new NullPointerException(msg);
    }

    /**
     * DESC: 抛出运行时异常
     * Created by jinphy, on 2018/1/3, at 19:56
     */
    public static void throwRuntime(String msg) {
        throw new RuntimeException(msg);
    }
}
