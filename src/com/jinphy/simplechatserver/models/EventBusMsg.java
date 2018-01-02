package com.jinphy.simplechatserver.models;

import com.jinphy.simplechatserver.constants.StringConst;
import com.jinphy.simplechatserver.controller.MyServer;
import org.greenrobot.eventbus.EventBus;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jinphy on 2017/12/5.
 */
public class EventBusMsg {

    /**
     * DESC: 存放用post方法请求时的请求参数信息
     * Created by jinphy, on 2018/1/2, at 11:21
     */
    private static Map<String, EventBusMsg> postMsgs = new ConcurrentHashMap<>();

    public MyServer server;
    public WebSocket client;
    public List<WebSocket> clients;
    public Request request;

    private EventBusMsg(MyServer server, WebSocket client, ClientHandshake handshake) {
        System.out.println();
        System.out.println("");
        this.server = server;
        this.client = client;
        this.clients = Arrays.asList(client);
        this.request = Request.parse(handshake);
        if (StringConst.GET.equals(this.request.getMethod())) {
            // get 请求则直接处理该请求
            EventBus.getDefault().post(this);
        } else {
            // post 请求则等到获取body后再处理该请求
            postMsgs.put(this.request.getRequestId(), this);
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
        new EventBusMsg(server,client, handshake);
    }

    /**
     * DESC: 处理post请求的请求体
     *
     * @param bodyStr 请求体
     * Created by jinphy, on 2018/1/2, at 11:33
     */
    public static void handle(String bodyStr) {
        Body body = Body.parse(bodyStr);
        EventBusMsg msg = get(body.getRequestId());
        if (msg != null) {
            msg.request.addParams(body.getContentMap());
            EventBus.getDefault().post(msg);
        }
    }

    public static EventBusMsg get(String requestId) {
        return postMsgs.remove(requestId);
    }
}
