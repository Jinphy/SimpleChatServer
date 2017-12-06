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
    public static void requareNonNull(Object object,String msg) throws MyNullPointerException {
        if (object == null) {
            throw new MyNullPointerException(msg);
        }
    }
}
