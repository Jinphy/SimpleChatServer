package com.jinphy.simplechatserver.controller;

import com.jinphy.simplechatserver.annotation.Path;
import com.jinphy.simplechatserver.models.Session;
import com.jinphy.simplechatserver.models.Response;
import com.jinphy.simplechatserver.utils.GsonUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * 该类用来处理网络请求接口
 * Created by jinphy on 2017/12/5.
 */
public class RequestController {

    /**
     * DESC: 网络请求接口map
     * Created by jinphy, on 2018/1/2, at 22:27
     */
    protected Map<String, Method> methodMap = new HashMap<>();

    /**
     * DESC: 单例持有类
     * Created by jinphy, on 2018/1/2, at 22:28
     */
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

    /**
     * DESC: 初始化
     * Created by jinphy, on 2018/1/2, at 22:28
     */
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
     * DESC: 加载网络请求接口
     * Created by jinphy, on 2017/12/7, at 0:27
     */
    protected void loadApiMethods() {
        Method[] methods = Api.class.getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers())                           // public
                    && Modifier.isPublic(method.getModifiers())                    // static
                    && method.getParameterCount() == 1                             // 参数只有一个
                    && method.getParameterTypes()[0] == Session.class          // 参数类型为EventBusMsg
                    && method.isAnnotationPresent(Path.class)) {                   // 注解了Path
                methodMap.put(method.getAnnotation(Path.class).path(), method);
            }
        }
    }


    /**
     * DESC: 接收EventBus的消息，然后分发到相应的网络请求接口
     * Created by jinphy, on 2017/12/7, at 0:32
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public synchronized void handleMsg(Session session) {
        Method method = methodMap.get(session.path());
        if (method == null) {
            Response response = new Response(Response.NO_API_NOT_FUND, "网络请求接口不存在！", null);
            session.server().broadcast(response.toString(), session.client());
            System.out.println("json: " + GsonUtils.toJson(response)+"\n");
            return;
        }
        try {
            method.invoke(null, session);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
