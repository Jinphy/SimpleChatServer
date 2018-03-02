package com.jinphy.simplechatserver.network.controller;

import com.jinphy.simplechatserver.database.dao.MessageDao;
import com.jinphy.simplechatserver.database.dao.UserDao;
import com.jinphy.simplechatserver.database.models.Result;
import com.jinphy.simplechatserver.models.db_models.AccessToken;
import com.jinphy.simplechatserver.models.db_models.Message;
import com.jinphy.simplechatserver.models.db_models.User;
import com.jinphy.simplechatserver.models.network_models.PushSession;
import com.jinphy.simplechatserver.network.MyServer;
import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.GsonUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DESC: 推送服务器控制器
 * Created by jinphy on 2018/1/16.
 */
public class PushServerController extends BaseController {

    private UserDao userDao;
    private MessageDao messageDao;

    /**
     * DESC: 推送服务器
     * Created by jinphy, on 2018/1/16, at 14:01
     */
    private MyServer pushServer;

    /**
     * DESC: 已经连接的客户端map
     *  key：已连接的客户端账号
     *  value: 账号对应的客户端
     * Created by jinphy, on 2018/1/16, at 14:00
     */
    private Map<String, WebSocket> clientMap = new ConcurrentHashMap<>();

    private static class InstanceHolder {
        static final PushServerController DEFAULT = new PushServerController();
    }


    /**
     * DESC: 获取单例
     * Created by jinphy, on 2018/1/16, at 13:58
     */
    public static PushServerController getInstance(MyServer pushServer) {
        InstanceHolder.DEFAULT.pushServer = pushServer;
        return InstanceHolder.DEFAULT;
    }

    /**
     * DESC: 初始化
     * Created by jinphy, on 2018/1/16, at 13:58
     */
    public static void init(MyServer pushServer) {
        getInstance(pushServer);
    }

    /**
     * DESC: 初始化
     * Created by jinphy, on 2018/1/16, at 13:57
     */
    private PushServerController() {
        userDao = UserDao.getInstance();
        messageDao = MessageDao.getInstance();
        EventBus.getDefault().register(this);
    }

    /**
     * DESC: 推送服务统一入口
     * Created by jinphy, on 2018/1/16, at 15:02
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleSession(PushSession session) {
        switch (session.task) {
            case acceptClient:
                handleAccept(session);
                break;
            case removeClient:
                handleRemove(session);
                break;
            case pushMessage:
                handlePush(session);
                break;
            default:
                break;
        }
    }


    /**
     * DESC: 处理push任务，调用该函数进行消息推送
     * Created by jinphy, on 2018/1/16, at 15:43
     */
    private void handlePush(PushSession session) {
        // 要推送的客户端连接
        List<WebSocket> clients;
        // 要推送的客户端账号
        String to;
        if (PushSession.ALL.equalsIgnoreCase(session.account)) {
            clients = new ArrayList<>(clientMap.size());
            for (Map.Entry<String, WebSocket> entry : clientMap.entrySet()) {
                WebSocket client = entry.getValue();
                if (client == null || client.isClosed()) {
                    continue;
                }
                clients.add(client);
            }
            to = PushSession.ALL;
        } else {
            WebSocket client = clientMap.get(session.account);
            if (client == null) {
                return;
            }
            if (client.isClosed()) {
                clientMap.remove(session.account);
                return;
            }
            clients = Arrays.asList(clientMap.get(session.account));
            to = session.account;
        }

        // 加载消息
        Result result = messageDao.loadMessage(to);

        // 推送消息
        if (result.count > 0) {
            System.out.println("------------push-----------");
            System.out.println("account: "+session.account);
            System.out.println("msg: " + GsonUtils.toJson(result.data));
            System.out.println();

            pushServer.broadcast(EncryptUtils.encodeThenEncrypt(GsonUtils.toJson(result.data)), clients);
        }

        // 更新消息
        messageDao.updateMessage(result.data);
    }

    /**
     * DESC: 处理添加连接任务，调用该函数添加一个新的推送连接
     * Created by jinphy, on 2018/1/16, at 15:44
     */
    public void handleAccept(PushSession session) {
        Result findResult = userDao.findUser(session.account);
        String serverAccessToken = findResult.first.get(User.ACCESS_TOKEN);
        String check = AccessToken.check(serverAccessToken, session.accessToken);
        if (check == AccessToken.OK) {
            clientMap.put(session.account, session.client);

            // 当有新客户端连接时要首先把没有接收到的消息推送给该客户端
            PushSession.pushMessage(session.account);
        } else {
            // 客户端登录已过期
            Message[] messages = new Message[1];
            messages[0] = new Message();
            messages[0].setContentType(Message.TYPE_SYSTEM_ACCOUNT_INVALIDATE);
            messages[0].setContent(check);
            messages[0].setCreateTime(System.currentTimeMillis()+"");
            messages[0].setFromAccount(User.SYSTEM);
            messages[0].setToAccount(session.account);
            pushServer.broadcast(EncryptUtils.encodeThenEncrypt(GsonUtils.toJson(messages)), Arrays.asList(session.client));
        }
    }

    /**
     * DESC: 处理删除连接任务，调用该函数删除一个推送连接
     * Created by jinphy, on 2018/1/16, at 15:44
     */
    public void handleRemove(PushSession session) {
        clientMap.remove(session.account);
    }
}
