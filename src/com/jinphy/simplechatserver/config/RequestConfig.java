package com.jinphy.simplechatserver.config;

/**
 * Created by jinphy on 2017/12/5.
 */
public interface RequestConfig {

    interface Path{
        String login = "/user/login";
        String findUser = "/user/findUser";
        String createNewUser = "/user/createNewUser";
    }


    interface Key{
        String account = "account";
        String password = "password";
        String date = "date";
        String deviceId = "deviceId";
    }
}
