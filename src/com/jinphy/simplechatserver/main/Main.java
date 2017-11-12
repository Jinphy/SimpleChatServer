package com.jinphy.simplechatserver.main;


import com.jinphy.simplechatserver.dao.UserDao;
import com.jinphy.simplechatserver.controller.MyServer;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static final int PUSH_SERVER_PORT = 4540;
    public static final int SEND_SERVER_PORT = 4541;
    public static final int COMMON_SERVER_PORT = 4542;

    private static MyServer pushServer;
    private static MyServer sendServer;
    private static MyServer commonServer;

    public static void main(String[] args) {
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
                    .doOnOpen((conn, handshake) -> {
                        // TODO: 2017/11/6 用户连接了服务器，如果是登录服务则要跟新登录状态
                        String descriptor = handshake.getResourceDescriptor();
                        handleDescriptor(descriptor);

                    })
                    .doOnMessage(new MyServer.OnMessage() {
                        @Override
                        public void onMessage(WebSocket conn, String message) {

                        }
                    })
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

    private static void handleDescriptor(String descriptor) {
        descriptor = descriptor.trim().substring(1);
        String[] split = descriptor.trim().split("/");
        if (split != null && split.length > 0) {
            switch (split[0]) {
                case "user":
                    handleUserPath(split);
                    break;
                case "send":
                    break;
                case "push":
                    break;

                default:
                    break;
            }
        }
    }

    private static void handleUserPath(String[] split) {
        String account;
        String password;
        switch (split[1]) {
            case "findUser":
                int index = split[2].indexOf("=") + 1;
                account = split[2].substring(index);
                try {
                    if (UserDao.getInstance().findUser(account)) {
                        commonServer.broadcast("yes");
                    } else {
                        commonServer.broadcast("no");
                    }
                } catch (Exception e) {
                    Thread.yield();
                    commonServer.broadcast("error");
                }
                break;
            case "createNewUser":
                Map<String, String> map = toMap(split[2]);
                System.out.println("account = " + map.get("account"));
                System.out.println("password = " + map.get("password"));
                System.out.print("date = " + map.get("date"));
                try {
                    if (UserDao.getInstance().createNewUser(
                            map.get("account"),
                            map.get("password"),
                            map.get("date"))) {
                        commonServer.broadcast("yes");
                    } else {
                        commonServer.broadcast("no");
                    }
                } catch (Exception e) {
                    commonServer.broadcast("error");
                }
                break;
        }
    }


    public static Map<String, String> toMap(String text) {
        text = text.substring(1);// 第一个字符时 "?"
        Map<String, String> map = new HashMap<>();
        String[] split = text.split("&");
        for (String s : split) {
            String[] item = s.split("=");
            map.put(item[0], item[1]);
        }
        return map;
    }

}
