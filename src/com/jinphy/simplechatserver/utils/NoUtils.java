package com.jinphy.simplechatserver.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * DESC: 好吗生成工具类
 * Created by jinphy on 2018/3/10.
 */
public class NoUtils {


    public static synchronized String generateGroupNo() {
        File file = new File("./config", "no.properties");

        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            Properties reader = new Properties();
            Properties writer = new Properties();
            in = new FileInputStream(file);
            reader.load(in);
            long groupNo = Long.valueOf(reader.getProperty("groupNo", "0"));
            in.close();
            out = new FileOutputStream(file);
            reader.setProperty("groupNo", (groupNo + 1) + "");
            reader.store(out,"This file store the next group no that will be create!");
            return "G_" + groupNo;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
