package com.jinphy.simplechatserver.models.network_models;

import com.jinphy.simplechatserver.models.db_models.User;
import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.Handshakedata;

/**
 * DESC:
 * Created by jinphy on 2018/1/16.
 */
public class PushSession {

    public static final String ALL = "ALL";

    public Task task;

    /**
     * DESC: 客户端与服务器连接的唯一id，由用户的账号经过加密组成
     * Created by jinphy, on 2018/1/16, at 14:31
     */
    public String account;

    public String accessToken;

    public WebSocket client;

    /**
     * DESC: 添加推送连接
     * Created by jinphy, on 2018/1/16, at 14:56
     */
    public static void acceptClient(WebSocket client, Handshakedata handshake) {
        PushSession session = new PushSession();
        session.client = client;
        session.task = Task.acceptClient;
        session.account = handshake.getFieldValue(User.ACCOUNT);
        session.accessToken = handshake.getFieldValue(User.ACCESS_TOKEN);
        EventBus.getDefault().post(session);
    }

    /**
     * DESC: 删除指定的推送连接
     * Created by jinphy, on 2018/1/16, at 14:56
     */
    public static void removeClient(WebSocket client) {
        PushSession session = new PushSession();
        session.task = Task.removeClient;
        session.account = parse(User.ACCOUNT, client.getResourceDescriptor());
        EventBus.getDefault().post(session);
    }


    /**
     * DESC: 向指定account推送消息
     * Created by jinphy, on 2018/1/18, at 16:20
     */
    public static void pushMessage(String account) {
        PushSession session = new PushSession();
        session.task = Task.pushMessage;
        session.account = account;
        EventBus.getDefault().post(session);
    }


    /**
     * DESC: 解析请求地址中的account字段
     * Created by jinphy, on 2018/1/16, at 14:56
     */
    public static String parse(String key, String descriptor) {
        if (descriptor.contains("/?content=")) {
            String content = descriptor.substring(descriptor.indexOf("/?content=") + 10);
            System.out.println("content = "+content);
            content = EncryptUtils.decryptThenDecode(content);
            return StringUtils.toMap(content).get(key);
        }

        return null;
    }


    public enum Task{

        /**
         * DESC: 添加新的推送连接
         * Created by jinphy, on 2018/1/16, at 14:15
         */
        acceptClient,

        /**
         * DESC: 删除指定的推送连接
         * Created by jinphy, on 2018/1/16, at 14:15
         */
        removeClient,

        /**
         * DESC: 推送消息
         * Created by jinphy, on 2018/1/16, at 14:16
         */
        pushMessage

    }
}
