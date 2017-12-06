package com.jinphy.simplechatserver.main;


import com.jinphy.simplechatserver.controller.RequestController;
import com.jinphy.simplechatserver.dao.UserDao;
import com.jinphy.simplechatserver.controller.MyServer;
import com.jinphy.simplechatserver.models.EventBusMsg;
import com.jinphy.simplechatserver.models.Response;
import com.jinphy.simplechatserver.models.UrlObject;
import com.jinphy.simplechatserver.utils.GsonUtils;
import com.jinphy.simplechatserver.utils.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.util.*;

public class Main {
    public static final int PUSH_SERVER_PORT = 4540;
    public static final int SEND_SERVER_PORT = 4541;
    public static final int COMMON_SERVER_PORT = 4542;

    private static MyServer pushServer;
    private static MyServer sendServer;
    private static MyServer commonServer;

    private static RequestController ontroller;

    public static void main(String[] args) {
        RequestController.init();

        Main.createPushServer();
        Main.createSendServer();
        Main.createCommonServer();
    }

    //    开启这个服务专门推送消息给客户端
    private static void createPushServer() {
        new Thread(() -> {
            pushServer = MyServer.newInstance(PUSH_SERVER_PORT)
                    .doOnStart(() -> System.out.println("push server start!"))
                    .doOnError((conn, ex) -> {
                        System.out.println("onError:-->" + conn.getRemoteSocketAddress());
                        ex.printStackTrace();
                    })
                    .doOnClose((conn, code, reason, remote) -> {
                        System.out.println("onClose:-->" + conn.getRemoteSocketAddress());
                        System.out.println("reason:-->" + reason);
                    });
            pushServer.start();
            while (true) {
                // TODO: 2017/11/6 把新消息推送给用户
            }
        }).start();
    }

    //    开启这个服务专门处理客户端发送的聊天信息
    private static void createSendServer() {
        new Thread(() -> {
            sendServer = MyServer.newInstance(SEND_SERVER_PORT)
                    .doOnStart(() -> System.out.println("send server start!"))
                    .doOnMessage(new MyServer.OnMessage() {
                        @Override
                        public void onMessage(WebSocket conn, String message) {
                            // TODO: 2017/11/6 接收并保存消息
                        }
                    })
                    .doOnError((conn, ex) -> {
                        System.out.println("onError:-->" + conn.getRemoteSocketAddress());
                        ex.printStackTrace();
                    })
                    .doOnClose((conn, code, reason, remote) -> {
                        System.out.println("onClose:-->" + conn.getRemoteSocketAddress());
                        System.out.println("reason:-->" + reason);
                    });
            sendServer.start();

        }).start();
    }

    //    开启这个服务专门处理通用的客户端请求
    private static void createCommonServer() {
        new Thread(() -> {
            commonServer = MyServer.newInstance(COMMON_SERVER_PORT)
                    .doOnStart(() -> System.out.println("send server start!"))
                    .doOnOpen(Main::onOpenOfCommonServer)
                    .doOnMessage((conn, message) -> { })
                    .doOnError((conn, ex) -> {
                        System.out.println("onError:-->" + conn.getRemoteSocketAddress());
                        ex.printStackTrace();
                    })
                    .doOnClose((conn, code, reason, remote) -> {
                        // TODO: 2017/11/6 用户断开了服务器，如果是登录服务则要跟新登录状态
                    });
            commonServer.start();
        }).start();

    }

    /**
     * DESC: 处理普通网络请求
     * Created by jinphy, on 2017/12/5, at 21:04
     */
    public static void onOpenOfCommonServer(WebSocket client, ClientHandshake handshake){
        EventBusMsg msg = EventBusMsg.create(commonServer,client, handshake);
        EventBus.getDefault().post(msg);
    }

//    private static void handleDescriptor(String descriptor,WebSocket client) {
//        System.out.println(descriptor);
//        List<WebSocket> clients = new ArrayList<>(1);
//        clients.add(client);
//        descriptor = descriptor.trim().substring(1);
//        String[] split = descriptor.trim().split("/");
//        if (split != null && split.length > 0) {
//            switch (split[0]) {
//                case "user":
//                    handleUserPath(split,clients);
//                    break;
//                case "send":
//                    break;
//                case "push":
//                    break;
//
//                default:
//                    break;
//            }
//        }
//    }

//    private static void handleUserPath(String[] split,Collection<WebSocket> clients) {
//        String account;
//        String password;
//        Map<String, String> map;
//        switch (split[1]) {
//            case "findUser":
//                System.out.println("findUser");
//                int index = split[2].indexOf("=") + 1;
//                account = split[2].substring(index);
//                try {
//                    if (UserDao.getInstance().findUser(account)) {
//                        Response response = new Response(Response.YES, "账号" + account + "存在", null);
//                        commonServer.broadcast(GsonUtils.toJson(response), clients);
//                    } else {
//                        Response response = new Response(Response.NO, "账号" + account + "不存在", null);
//                        commonServer.broadcast(GsonUtils.toJson(response), clients);
//                    }
//                } catch (Exception e) {
//                    Thread.yield();
//                    commonServer.broadcast("error",clients);
//                }
//                break;
//            case "createNewUser":
//                System.out.println("createNewUser");
//                map = StringUtils.toMap(split[2]);
//                System.out.println("account = " + map.get("account"));
//                System.out.println("password = " + map.get("password"));
//                System.out.print("date = " + map.get("date"));
//                try {
//                    if (UserDao.getInstance().createNewUser(
//                            map.get("account"),
//                            map.get("password"),
//                            map.get("date"))) {
//                        commonServer.broadcast("yes",clients);
//                    } else {
//                        commonServer.broadcast("no",clients);
//                    }
//                } catch (Exception e) {
//                    commonServer.broadcast("error",clients);
//                }
//                break;
//            case "login":
//                System.out.println("login");
//                map = StringUtils.toMap(split[2]);
//                System.out.println("account = " + map.get("account"));
//                System.out.println("password = " + map.get("password"));
//                System.out.println("deviceId = "+map.get("deviceId"));
//                try {
//                    if (UserDao.getInstance().login(
//                            map.get("account"),
//                            map.get("password"),
//                            map.get("deviceId")) ) {
//                        commonServer.broadcast("yes",clients);
//                    } else {
//                        commonServer.broadcast("no",clients);
//                    }
//                } catch (Exception e) {
//                    commonServer.broadcast("error",clients);
//                    e.printStackTrace();
//                }
//                break;
//        }
//    }


}
