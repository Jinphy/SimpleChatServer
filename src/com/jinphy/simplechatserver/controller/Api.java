package com.jinphy.simplechatserver.controller;

import com.jinphy.simplechatserver.MyNullPointerException;
import com.jinphy.simplechatserver.annotation.Path;
import com.jinphy.simplechatserver.config.RequestConfig;
import com.jinphy.simplechatserver.dao.UserDao;
import com.jinphy.simplechatserver.models.EventBusMsg;
import com.jinphy.simplechatserver.models.Response;
import com.jinphy.simplechatserver.utils.GsonUtils;
import com.jinphy.simplechatserver.utils.ObjectHelper;

import java.sql.SQLException;

/**
 * DESC: 网络请求接口处理类，在这里声明并实现各个接口的调用方法
 *
 *  方法声明规则：
 *      1、 public
 *      2、 static
 *      3、 注解Path
 *      4、 只有一个EventBusMsg 类型的参数
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
    public static void login(EventBusMsg msg) {
        System.out.println(RequestConfig.Path.login);
        Response response = null;
        try {
            String account = msg.request.getParams().get(RequestConfig.Key.account);
            String paramError = "参数不完整！";
            ObjectHelper.requareNonNull(account, paramError);
            String password = msg.request.getParams().get(RequestConfig.Key.password);
            ObjectHelper.requareNonNull(account, paramError);
            String deviceId = msg.request.getParams().get(RequestConfig.Key.deviceId);
            ObjectHelper.requareNonNull(account, paramError);

            if (UserDao.getInstance().login(account, password, deviceId)) {
                response = new Response(Response.YES, "登录成功！", null);
            } else {
                response = new Response(Response.NO_LOGIN, "密码错误，请重新输入！", null);
            }

        } catch (MyNullPointerException e) {
            response = new Response(Response.NO_PARAMS_MISSING, e.getMessage(), null);
        } catch (InterruptedException | SQLException e) {
            response = new Response(Response.NO_SERVER, "服务器异常，请稍后再试！", null);
        } finally {
            msg.server.broadcast(response.toString(), msg.clients);
            System.out.println("json: "+ GsonUtils.toJson(response));
        }

    }


    /**
     * DESC: 查询用户是否存在请求
     * Created by jinphy, on 2017/12/5, at 21:54
     */
    @Path(path = RequestConfig.Path.findUser)
    public static void findUser(EventBusMsg msg) {
        System.out.println(RequestConfig.Path.findUser);
        Response response = null;
        try {
            String account = msg.request.getParams().get(RequestConfig.Key.account);
            ObjectHelper.requareNonNull(account, "参数不完整！");
            if (UserDao.getInstance().findUser(account)) {
                response = new Response(Response.YES, "账号" + account + "存在", null);
            } else {
                response = new Response(Response.NO_FIND_USER, "账号" + account + "不存在,\n请重新输入！", null);
            }
        } catch (MyNullPointerException e) {
            response = new Response(Response.NO_PARAMS_MISSING, e.getMessage(), null);
        } catch (SQLException | InterruptedException e) {
            Thread.yield();
            response = new Response(Response.NO_SERVER, "服务器异常，请稍后再试！", null);
        } finally {
            msg.server.broadcast(response.toString(), msg.clients);
            System.out.println("json: "+GsonUtils.toJson(response));
        }
    }


    /**
     * DESC: 创建新用户请求
     * Created by jinphy, on 2017/12/5, at 21:54
     */
    @Path(path = RequestConfig.Path.createNewUser)
    public static void createNewUser(EventBusMsg msg) {
        System.out.println(RequestConfig.Path.createNewUser);
        Response response = null;
        String paramError = "参数不完整！";
        try {
            String account = msg.request.getParams().get(RequestConfig.Key.account);
            ObjectHelper.requareNonNull(account, paramError);
            String password = msg.request.getParams().get(RequestConfig.Key.password);
            ObjectHelper.requareNonNull(account, paramError);
            String date = msg.request.getParams().get(RequestConfig.Key.date);
            ObjectHelper.requareNonNull(account, paramError);
            UserDao.getInstance().createNewUser(account, password, date);
            response = new Response(Response.YES, "恭喜您，账号注册成功！", null);
        } catch (MyNullPointerException e) {
            response = new Response(Response.NO_PARAMS_MISSING, e.getMessage(), null);
        } catch (InterruptedException | SQLException e) {
            response = new Response(Response.NO_SERVER, "服务器异常，请稍后再试！", null);
        } finally {
            msg.server.broadcast(response.toString(), msg.clients);
            System.out.println("json: "+GsonUtils.toJson(response));
        }
    }

}
