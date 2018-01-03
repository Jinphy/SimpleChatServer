package com.jinphy.simplechatserver.models;

import com.jinphy.simplechatserver.constants.StringConst;
import com.jinphy.simplechatserver.controller.MyServer;
import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DESC: 一个http请求回话，保存着单次网络请求的连接信息
 *
 *
 * Created by jinphy on 2017/12/5.
 */
public class Session {

    /**
     * DESC: 存放用post方法请求时的请求参数信息
     * Created by jinphy, on 2018/1/2, at 11:21
     */
    private static Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    private MyServer server;               // 服务端
    private List<WebSocket> clients;       // 客户端
    private String method;                // 请求方法
    private String requestId;             // 每次请求的唯一id,就算是同一台设备请求同一个接口也不会相同
    private String path;                  // 请求接口
    private Map<String,String> params;    // 请求参数，包括url中的和body中的（如果有的话）

    private Session(MyServer server, WebSocket client, ClientHandshake handshake) {
        this.server(server);
        this.client(client);
        this.parseHandshake(handshake);

        if (StringConst.GET.equals(this.method)) {
            // get 请求则直接处理该请求
            EventBus.getDefault().post(this);
        } else {
            // post 请求则等到获取body后再处理该请求
            sessionMap.put(this.requestId, this);
        }
    }


    /**
     * DESC: 处理网络连接时的请求
     *  分post和get两种
     *  1、get请求则立即处理请求
     *  2、post请求则等到获取请求体之后再处理请求
     * Created by jinphy, on 2018/1/2, at 11:34
     */
    public static void handle(MyServer server, WebSocket client, ClientHandshake handshake) {
        new Session(server,client, handshake);
    }

    /**
     * DESC: 处理post请求的请求体
     *
     * @param bodyStr 请求体
     * Created by jinphy, on 2018/1/2, at 11:33
     */
    public static void handle(String bodyStr) {
        Body body = Body.parse(bodyStr);
        Session session = get(body.getRequestId());
        if (session != null) {
            session.addParams(body.getContentMap());
            EventBus.getDefault().post(session);
        }
    }

    /**
     * DESC: 根据请求id获取Session实例
     * Created by jinphy, on 2018/1/3, at 11:08
     */
    public static Session get(String requestId) {
        return sessionMap.remove(requestId);
    }

    /**
     * DESC: 解析 handshake
     *
     *  description：/user/findUser/?content=JIEFFIHGANVIAEFAIJGEFAIAOHAIGI
     *  content的值是一个编码、加密后的字符串
     *  path = /user/findUser
     *  content = JIEFFIHGANVIAEFAIJGEFAIAOHAIGI
     *  params: account=15889622379->toMap
     *
     * Created by jinphy, on 2017/12/5, at 20:54
     */
    public void parseHandshake(ClientHandshake handshake){
        String descriptor = handshake.getResourceDescriptor();
        String[] split = descriptor.split("/\\?content=");
        String path = split[0];
        String content = "";
        if (split.length > 1) {
            content = split[1];
        }
        // 解密和反编码
        content = EncryptUtils.decryptThenDecode(content);

        this.method = handshake.getFieldValue("method").toUpperCase();
        this.requestId = handshake.getFieldValue("requestId");
        this.path = path;
        this.params = StringUtils.toMap(content);
        System.out.println("method: "+ method);
        System.out.println("description: " + descriptor);
        System.out.println("requestId: " + requestId);
        System.out.println("path: " + path);
        System.out.println("content: " + content);
    }

    public MyServer server() {
        return server;
    }

    public void server(MyServer server) {
        this.server = server;
    }

    public List<WebSocket> client() {
        return clients;
    }

    public void client(WebSocket clients) {
        if (this.clients == null) {
            this.clients = new ArrayList<>();
        }
        this.clients.add(clients);
    }

    public String method() {
        return method;
    }

    public void method(String method) {
        this.method = method;
    }

    /**
     * DESC: 获取请求路径
     * Created by jinphy, on 2017/12/5, at 20:52
     */
    public String path() {
        return path;
    }

    /**
     * DESC: 获取请求参数
     * Created by jinphy, on 2017/12/5, at 20:52
     */
    public Map<String, String> params() {
        return params;
    }

    public void path(String path) {
        this.path = path;
    }

    public String requestId() {
        return requestId;
    }

    public void requestId(String requestId) {
        this.requestId = requestId;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void addParams(Map<String, String> params) {
        this.params.putAll(params);
    }
}
