package com.jinphy.simplechatserver.config;

/**
 * Created by jinphy on 2017/12/5.
 */
public interface RequestConfig {

    /**
     * DESC: 网络请求接口
     * Created by jinphy, on 2018/1/2, at 12:36
     */
    interface Path{
        /**
         * DESC: 登录接口
         *  请求方式：Post
         *  参数：
         *      1、account:         必传，登录账号
         *      2、password：       非必传，登录密码，md5加密，不传时需要用验证码验证登录
         *      3、deviceId:        必传：设备唯一识别码，md5加密
         *
         * Created by jinphy, on 2018/1/2, at 12:35
         */
        String login = "/user/login";

        /**
         * DESC: 查找用户是否穿在接口
         *  请求方式：Get
         *  参数：
         *      1、account：        必传，账号
         *
         * Created by jinphy, on 2018/1/2, at 12:43
         */
        String findUser = "/user/findUser";

        /**
         * DESC: 创建用户接口
         *  请求方式：Post
         *  参数：
         *      1、account:         必传：要创建的账号
         *      2、password:        必传：用户密码
         *      3、date:            必传：创建日期
         * Created by jinphy, on 2018/1/2, at 12:47
         */
        String createNewUser = "/user/createNewUser";
    }


    interface Key{
        String account = "account";
        String password = "password";
        String date = "date";
        String deviceId = "deviceId";
    }
}
