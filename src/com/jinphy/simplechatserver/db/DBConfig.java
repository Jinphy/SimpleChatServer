package com.jinphy.simplechatserver.db;

import java.io.*;
import java.util.Properties;

public class DBConfig {
    String driver;
    String url;
    String user;
    String password;

    int minConnectionNum;
    int maxConnectionNum;
    long connectTimeout;
    long connectIdleTime;

    public DBConfig(){
        Properties properties = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream("config/mysql.properties"));
            properties.load(in);
            this.driver = properties.getProperty("driver");
            this.url = properties.getProperty("url");
            this.user = properties.getProperty("user");
            this.password = properties.getProperty("password");
            this.minConnectionNum = Integer.valueOf(properties.getProperty("minConnectionNum"));
            this.maxConnectionNum = Integer.valueOf(properties.getProperty("maxConnectionNum"));
            this.connectTimeout = Long.valueOf(properties.getProperty("connectTimeout"));
            this.connectIdleTime = Long.valueOf(properties.getProperty("connectIdleTime"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
