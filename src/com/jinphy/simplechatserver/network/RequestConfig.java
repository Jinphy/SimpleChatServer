package com.jinphy.simplechatserver.network;

/**
 * Created by jinphy on 2017/12/5.
 */
public interface RequestConfig {

    /**
     * DESC: 网络请求接口
     * Created by jinphy, on 2018/1/2, at 12:36
     */
    interface Path{

        // 登录接口
        String login = "/user/login";

        // 登出接口
        String logout = "/user/logout";

        // 查询账号是否存在接口
        String findUser = "/user/findUser";

        // 注册接口
        String signUp = "/user/signUp";

        // 修改用户信息接口
        String modifyUserInfo = "/user/modifyUserInfo";
    }


    interface Key{
        String account = "account";
        String password = "password";
        String date = "date";
        String deviceId = "deviceId";
        String accessToken = "accessToken";
    }
}
