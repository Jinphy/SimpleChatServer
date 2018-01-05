package com.jinphy.simplechatserver.main;


import com.jinphy.simplechatserver.network.RequestController;
import com.jinphy.simplechatserver.network.MyServer;
import com.jinphy.simplechatserver.models.network_models.Session;
import org.java_websocket.WebSocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static final int PUSH_SERVER_PORT = 4540;
    public static final int SEND_SERVER_PORT = 4541;
    public static final int COMMON_SERVER_PORT = 4542;

    private static MyServer pushServer;
    private static MyServer sendServer;
    private static MyServer commonServer;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(3);

    private static RequestController ontroller;

    public static void main(String[] args) {
        RequestController.init();

        Main.createPushServer();
        Main.createSendServer();
        Main.createCommonServer();
    }

    //    开启这个服务专门推送消息给客户端
    private static void createPushServer() {
        threadPool.execute(()->{
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
        });
    }

    //    开启这个服务专门处理客户端发送的聊天信息
    private static void createSendServer() {
        threadPool.execute(()->{
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

        });
    }

    //    开启这个服务专门处理通用的客户端请求
    private static void createCommonServer() {
        threadPool.execute(()->{
            commonServer = MyServer.newInstance(COMMON_SERVER_PORT)
                    .doOnStart(() -> System.out.println("send server start!"))
                    .doOnOpen((client, handshake) -> {
                        Session.handle(commonServer,client,handshake);
                    })
                    .doOnMessage((conn, message) -> {
                        Session.handle(message);
                    })
                    .doOnError((conn, ex) -> {
                        System.out.println("onError:-->" + conn.getRemoteSocketAddress());
                        ex.printStackTrace();
                    })
                    .doOnClose((conn, code, reason, remote) -> {
                        // TODO: 2017/11/6 用户断开了服务器，如果是登录服务则要跟新登录状态
                    });
            commonServer.start();
        });

    }


}
