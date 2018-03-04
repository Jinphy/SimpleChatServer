package com.jinphy.simplechatserver.network.controller;

import com.jinphy.simplechatserver.database.dao.MessageDao;
import com.jinphy.simplechatserver.database.models.Result;
import com.jinphy.simplechatserver.models.network_models.PushSession;
import com.jinphy.simplechatserver.models.network_models.SendSession;
import com.jinphy.simplechatserver.network.MyServer;
import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.GsonUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

/**
 * DESC:
 * Created by jinphy on 2018/1/18.
 */
public class SendServerController {

    public static final String OK = "200";
    public static final String NO = "100";

    private MyServer sendServer;

    private MessageDao messageDao;


    private static class InstanceHolder {
        static final SendServerController DEFAULT = new SendServerController();
    }

    public static SendServerController getInstance(MyServer sendServer) {
        InstanceHolder.DEFAULT.sendServer = sendServer;
        return InstanceHolder.DEFAULT;
    }

    public static void init(MyServer sendServer) {
        getInstance(sendServer);
    }

    private SendServerController() {
        messageDao = MessageDao.getInstance();
        EventBus.getDefault().register(this);
    }


    /**
     * DESC: 接收客户端发来的消息并主动推送给接收该信息的用户
     * Created by jinphy, on 2018/1/18, at 16:21
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleSession(SendSession session) {
        Result result = messageDao.saveMessage(session.message);
        Map<String, String> map = new HashMap<>();
        map.put("sendId", session.sendId);
        if (result.count == 1) {
            // 消息保存成功，返回发送消息成功的回执
            map.put("code", OK);
            // 推送消息给指定的用户
            PushSession.pushMessage(session.message.getToAccount());
        } else {
            // 消息保存失败，返回发送消息失败的回执
            map.put("code", NO);
        }

        // 发送消息回执
        String response = GsonUtils.toJson(map);
        sendServer.broadcast(EncryptUtils.encodeThenEncrypt(response), session.client);
        System.out.println("send response: " + response);
    }
}
