package com.jinphy.simplechatserver.controller;

import com.jinphy.simplechatserver.MyNullPointerException;
import com.jinphy.simplechatserver.annotation.Path;
import com.jinphy.simplechatserver.config.RequestConfig;
import com.jinphy.simplechatserver.dao.UserDao;
import com.jinphy.simplechatserver.models.EventBusMsg;
import com.jinphy.simplechatserver.models.Response;
import com.jinphy.simplechatserver.utils.GsonUtils;
import com.jinphy.simplechatserver.utils.ObjectHelper;
import com.jinphy.simplechatserver.utils.StringUtils;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 该类用来处理网络请求接口
 * Created by jinphy on 2017/12/5.
 */
public class RequestController {

    protected Map<String, Method> methodMap = new HashMap<>();

    private static class RequestControllerHolder {
        static final RequestController DAFAULT = new RequestController();
    }

    /**
     * DESC: 单例模式
     * Created by jinphy, on 2017/12/5, at 22:06
     */
    public static RequestController getInstance() {
        if (!EventBus.getDefault().isRegistered(RequestControllerHolder.DAFAULT)) {
            EventBus.getDefault().register(RequestControllerHolder.DAFAULT);
        }
        return RequestControllerHolder.DAFAULT;
    }

    public static void init() {
        getInstance();
    }


    /*
     * DESC: 私有化
     * Created by jinphy, on 2017/12/5, at 22:09
     */
    public RequestController() {
        EventBus.getDefault().register(this);
        loadApiMethods();
    }

    /**
     * DESC: 获取该类的所有注释了Path注解的方法
     * Created by jinphy, on 2017/12/7, at 0:27
     */
    protected void loadApiMethods(){
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Path.class)) {
                methodMap.put(method.getAnnotation(Path.class).path(), method);
            }
        }
    }


    /**
     * DESC: 接收EventBus的消息，然后分发到相应的网络请求接口
     * Created by jinphy, on 2017/12/7, at 0:32
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleMsg(EventBusMsg msg) {
        Method method = methodMap.get(msg.path);
        if (method ==null) {
            Response response = new Response(Response.NO, "网络请求接口不存在！", null);
            msg.server.broadcast(GsonUtils.toJson(response), msg.clients);
            System.out.println("json: "+GsonUtils.toJson(response));
            return;
        }
        try {
            method.invoke(this, msg);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    //================================================================================\\
    //********************************************************************************\\

    /**
     * DESC: 登录请求
     * Created by jinphy, on 2017/12/5, at 21:54
     */
    @Path(path = RequestConfig.Path.login)
    public void login(EventBusMsg msg) {
        System.out.println(RequestConfig.Path.login);
        Response response = null;
        try {
            String account = msg.params.get(RequestConfig.Key.account);
            String paramError = "参数不完整！";
            ObjectHelper.requareNonNull(account, paramError);
            String password = msg.params.get(RequestConfig.Key.password);
            ObjectHelper.requareNonNull(account, paramError);
            String deviceId = msg.params.get(RequestConfig.Key.deviceId);
            ObjectHelper.requareNonNull(account, paramError);

            if (UserDao.getInstance().login(account, password, deviceId)) {
                response = new Response(Response.YES, "登录成功！", null);
            } else {
                response = new Response(Response.NO, "密码错误，请重新输入！", null);
            }

        } catch (MyNullPointerException e) {
            response = new Response(Response.NO, e.getMessage(), null);
        } catch (InterruptedException | SQLException e) {
            response = new Response(Response.NO, "服务器异常，请稍后再试！", null);
        } finally {
            msg.server.broadcast(GsonUtils.toJson(response), msg.clients);
            System.out.println("json: "+GsonUtils.toJson(response));
        }

    }


    /**
     * DESC: 查询用户是否存在请求
     * Created by jinphy, on 2017/12/5, at 21:54
     */
    @Path(path = RequestConfig.Path.findUser)
    public void findUser(EventBusMsg msg) {
        System.out.println(RequestConfig.Path.findUser);
        Response response = null;
        try {
            String account = msg.params.get(RequestConfig.Key.account);
            ObjectHelper.requareNonNull(account, "参数不完整！");
            if (UserDao.getInstance().findUser(account)) {
                response = new Response(Response.YES, "账号" + account + "存在", null);
            } else {
                response = new Response(Response.NO, "账号" + account + "不存在,请重新输入！", null);
            }
        } catch (MyNullPointerException e) {
            response = new Response(Response.NO, e.getMessage(), null);
        } catch (SQLException | InterruptedException e) {
            Thread.yield();
            response = new Response(Response.NO, "服务器异常，请稍后再试！", null);
        } finally {
            msg.server.broadcast(GsonUtils.toJson(response), msg.clients);
            System.out.println("json: "+GsonUtils.toJson(response));
        }
    }


    /**
     * DESC: 创建新用户请求
     * Created by jinphy, on 2017/12/5, at 21:54
     */
    @Path(path = RequestConfig.Path.createNewUser)
    public void createNewUser(EventBusMsg msg) {
        System.out.println(RequestConfig.Path.createNewUser);
        Response response = null;
        String paramError = "参数不完整！";
        try {
            String account = msg.params.get(RequestConfig.Key.account);
            ObjectHelper.requareNonNull(account, paramError);
            String password = msg.params.get(RequestConfig.Key.password);
            ObjectHelper.requareNonNull(account, paramError);
            String date = msg.params.get(RequestConfig.Key.date);
            ObjectHelper.requareNonNull(account, paramError);
            UserDao.getInstance().createNewUser(account, password, date);
            response = new Response(Response.YES, "恭喜您，账号注册成功！", null);
        } catch (MyNullPointerException e) {
            response = new Response(Response.NO, e.getMessage(), null);
        } catch (InterruptedException | SQLException e) {
            response = new Response(Response.NO, "服务器异常，请稍后再试！", null);
        } finally {
            msg.server.broadcast(GsonUtils.toJson(response), msg.clients);
            System.out.println("json: "+GsonUtils.toJson(response));
        }
    }


}
