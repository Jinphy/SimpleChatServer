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

        // 添加好友接口
        String addFriend = "/friend/addFriend";

        // 加载指定账号对应的所有好友接口
        String loadFriends = "/friend/loadFriends";

        // 获取指定的好友
        String getFriend = "/friend/getFriend";

        // 获取指定账号头像接口
        String getAvatar = "/user/getAvatar";

        // 修改好友信息
        String modifyRemark = "/friend/modifyRemark";

        // 修改好友状态
        String modifyStatus = "/friend/modifyStatus";

        // 删除好友
        String deleteFriend = "/friend/deleteFriend";
    }

    interface Key{
        String account = "account";
        String password = "password";
        String date = "date";
        String deviceId = "deviceId";
        String accessToken = "accessToken";
        String requestAccount = "requestAccount";
        String receiveAccount = "receiveAccount";
        String remark = "remark";
        String verifyMsg = "verifyMsg";
        String confirm = "confirm";
    }
}
