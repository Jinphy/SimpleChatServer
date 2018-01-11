package com.jinphy.simplechatserver.network;

import com.jinphy.simplechatserver.annotation.Path;
import com.jinphy.simplechatserver.database.dao.UserDao;
import com.jinphy.simplechatserver.database.models.Result;
import com.jinphy.simplechatserver.models.db_models.AccessToken;
import com.jinphy.simplechatserver.models.db_models.User;
import com.jinphy.simplechatserver.models.network_models.KeyValueArray;
import com.jinphy.simplechatserver.models.network_models.Session;
import com.jinphy.simplechatserver.models.network_models.Response;
import com.jinphy.simplechatserver.utils.GsonUtils;
import com.jinphy.simplechatserver.utils.StringUtils;

import java.util.Map;

import static com.jinphy.simplechatserver.constants.StringConst.LINE;
import static com.jinphy.simplechatserver.models.network_models.Response.*;

/**
 * DESC: 网络请求接口处理类，在这里声明并实现各个接口的调用方法
 *
 *  方法声明规则：
 *      1、 public
 *      2、 static
 *      3、 注解Path
 *      4、 只有一个Session 类型的参数
 * Created by jinphy on 2018/1/2.
 */
public class Api {

    //================================================================================\\
    //*********************接口调用方法  ***********************************************\\

    /**
     * DESC: 登录请求
     * Created by jinphy, on 2017/12/5, at 21:54
     */
    @Path(path = RequestConfig.Path.login)
    public static void login(Session session) {
        String code;
        String msg;
        String account = session.params().get(RequestConfig.Key.account);
        String password = session.params().get(RequestConfig.Key.password);
        String deviceId = session.params().get(RequestConfig.Key.deviceId);
        Result result = null;
        if (StringUtils.isTrimEmpty(account)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            // 查询数据库
            result = UserDao.getInstance().login(account, password, deviceId);
            session.loggoer.append(result.logger + LINE);
            if (result.count < 0) {
                code = NO_SERVER;
                msg = "服务器异常，请稍后再试！";
            } else if (result.count == 0) {
                code = NO_FIND_USER;
                msg = "账号" + account + "不存在,\n请重新输入！";
            } else {
                if (result.data != null) {
                    code = YES;
                    msg = "登录成功！";
                } else {
                    code = NO_LOGIN;
                    msg = "密码错误，请重新输入！";
                }
            }
        }


        Response response = Response.make(code, msg, result == null ? null : result.first);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) +LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);

    }


    /**
     * DESC: 查询用户是否存在请求
     * Created by jinphy, on 2017/12/5, at 21:54
     */
    @Path(path = RequestConfig.Path.findUser)
    public static void findUser(Session session) {
        String code;
        String msg;
        String account = session.params().get(RequestConfig.Key.account);
        if (StringUtils.isTrimEmpty(account)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            Result result = UserDao.getInstance().findUser(account);
            session.loggoer.append(result.logger+ LINE);
            if (result.count < 0) {
                code = NO_SERVER;
                msg = "服务器异常，请稍后再试！";
            } else if (result.count == 0) {
                code = NO_FIND_USER;
                msg = "账号" + account + "不存在,\n请重新输入！";
            } else {
                code = YES;
                msg = "账号" + account + "存在";
            }
        }
        Response response = Response.make(code, msg, null);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }


    /**
     * DESC: 创建新用户请求
     * Created by jinphy, on 2017/12/5, at 21:54
     */
    @Path(path = RequestConfig.Path.signUp)
    public static void createNewUser(Session session) {
        String code;
        String msg;

        String account = session.params().get(RequestConfig.Key.account);
        String password = session.params().get(RequestConfig.Key.password);
        String date = session.params().get(RequestConfig.Key.date);
        if (StringUtils.isTrimEmpty(account,password,date)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            Result result = UserDao.getInstance().createNewUser(account,password,date);
            session.loggoer.append(result.logger+LINE);
            if (result.count <0) {
                code = NO_SERVER;
                msg = "服务器异常，请稍后再试！";
            } else if (result.count == 0) {
                code = NO_PARAMS_ERROR;
                msg = "请求参数错误，请重新检查";
            } else {
                code = YES;
                msg = "恭喜您，账号注册成功！";
            }
        }
        Response response = Response.make(code, msg, null);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }


    @Path(path = RequestConfig.Path.modifyUserInfo)
    public static void modifyUserInfo(Session session) {
        String code;
        String msg;
        Map<String,String> data = null;

        String account = session.params().remove(RequestConfig.Key.account);
        String accessToken = session.params().remove(RequestConfig.Key.accessToken);
        String deviceId = session.params().remove(RequestConfig.Key.deviceId);
        if (StringUtils.isTrimEmpty(account, deviceId, accessToken)) {
            code = NO_PARAMS_MISSING;
            msg = "参数不完整！";
        } else {
            Result findResult = UserDao.getInstance().findUser(account);
            String check = AccessToken.check(findResult.first.get(User.ACCESS_TOKEN), accessToken);
            if (check != AccessToken.OK) {
                // 令牌过期
                code = NO_ACCESS_TOKEN;
                msg = check;
            } else {
                // 参数正确

                // 更新AccessToken
                session.params().put(User.ACCESS_TOKEN, AccessToken.make(deviceId, User.STATUS_LOGIN).toString());

                // 解析参数
                KeyValueArray array = KeyValueArray.parse(session.params());

                // 更新数据库，更新时account和deviceId要去除
                Result result = UserDao.getInstance().updateUser(account, array.keys, array.values);

                session.loggoer.append(result.logger + LINE);
                if (result.count < 0) {
                    code = NO_SERVER;
                    msg = "服务器异常，请稍后再试！";
                } else if (result.count == 0) {
                    code = NO_PARAMS_ERROR;
                    msg = "请求参数错误，请重新检查";
                } else {
                    // TODO: 2018/1/10 获取更新结果
                    code = YES;
                    msg = "个人信息修改成功！";
                    // 更新成功后要把account加回来，并且把password删除
                    // 所以最终返回给客户端的参数比入参少了deviceId和password两个字段
                    session.params().put(User.ACCOUNT, account);
                    session.params().remove(User.PASSWORD);
                    data = session.params();
                }
            }
        }
        Response response = Response.make(code, msg, data);
        session.server().broadcast(response.toString(), session.client());
        session.loggoer.append("response json: " + GsonUtils.toJson(response) + LINE)
                .append("=================网络请求结束=====================================================================\n\n");
        System.out.println(session.loggoer);
    }
}
