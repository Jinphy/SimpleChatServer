package com.jinphy.simplechatserver.models.network_models;

import com.google.gson.reflect.TypeToken;
import com.jinphy.simplechatserver.models.db_models.Message;
import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.GsonUtils;
import org.greenrobot.eventbus.EventBus;
import org.java_websocket.WebSocket;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * DESC: 接收用户发送消息的回话，处理用户消息的发送
 * Created by jinphy on 2018/1/18.
 */
public class SendSession {

    private static Type type = new TypeToken<Map<String, String>>() {
    }.getType();


    // 发送消息的客户端，用来接收发送回执
    public List<WebSocket> client;

    // 消息的发送唯一识别码
    public String sendId;

    // 用户发来的消息
    public Message message;

    /**
     * DESC: 处理用户发送过来的消息
     * Created by jinphy, on 2018/1/18, at 16:25
     */
    public static void handle(WebSocket client, String msg) {

        msg = EncryptUtils.decryptThenDecode(msg);

        Map<String, String> map = GsonUtils.toBean(msg, type);

        SendSession session = new SendSession();
        session.sendId = map.get("sendId");
        session.message = new Message();
        session.message.setFromAccount(map.get(Message.FROM));
        session.message.setToAccount(map.get(Message.TO));
        session.message.setCreateTime(map.get(Message.CREATE_TIME));
        session.message.setContent(map.get(Message.CONTENT));
        session.message.setContentType(map.get(Message.CONTENT_TYPE));
        session.client = Arrays.asList(client);

        EventBus.getDefault().post(session);
    }
}
