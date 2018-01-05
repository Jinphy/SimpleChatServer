package com.jinphy.simplechatserver.network;

import com.jinphy.simplechatserver.MyNullPointerException;
import com.jinphy.simplechatserver.annotation.Path;
import com.jinphy.simplechatserver.constants.StringConst;
import com.jinphy.simplechatserver.database.dao.UserDao;
import com.jinphy.simplechatserver.database.models.Result;
import com.jinphy.simplechatserver.models.db_models.User;
import com.jinphy.simplechatserver.models.network_models.Session;
import com.jinphy.simplechatserver.models.network_models.Response;
import com.jinphy.simplechatserver.utils.GsonUtils;
import com.jinphy.simplechatserver.utils.ObjectHelper;
import com.jinphy.simplechatserver.utils.StringUtils;

import java.lang.reflect.Array;
import java.sql.SQLException;

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
    @Path(path = RequestConfig.Path.createNewUser)
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

}
