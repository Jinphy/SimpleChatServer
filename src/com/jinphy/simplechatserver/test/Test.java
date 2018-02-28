package com.jinphy.simplechatserver.test;

import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * DESC:
 * Created by jinphy on 2018/1/3.
 */
public class Test {

    public static void main(String[] args) throws UnsupportedEncodingException {
        File file = new File("./files");
        file.mkdirs();
        System.out.println(file.getAbsolutePath());
    }

}
