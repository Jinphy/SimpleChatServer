package com.jinphy.simplechatserver.main;


import com.jinphy.simplechatserver.models.network_models.FileSession;
import com.jinphy.simplechatserver.models.network_models.PushSession;
import com.jinphy.simplechatserver.models.network_models.SendSession;
import com.jinphy.simplechatserver.network.controller.CommonServerController;
import com.jinphy.simplechatserver.network.MyServer;
import com.jinphy.simplechatserver.models.network_models.CommonSession;
import com.jinphy.simplechatserver.network.controller.FileServerController;
import com.jinphy.simplechatserver.network.controller.PushServerController;
import com.jinphy.simplechatserver.network.controller.SendServerController;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static final int PUSH_SERVER_PORT = 4540;
    public static final int SEND_SERVER_PORT = 4541;
    public static final int COMMON_SERVER_PORT = 4542;
    public static final int FILE_SERVER_PORT = 4543;

    private static MyServer pushServer;
    private static MyServer sendServer;
    private static MyServer commonServer;
    private static MyServer fileServer;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(3);

    public static void main(String[] args) {
        Main.createPushServer();
//        Main.createSendServer();
        Main.createCommonServer();
        Main.createFileServer();
    }

    //    开启这个服务专门推送消息给客户端
    private static void createPushServer() {
        threadPool.execute(()->{
            pushServer = MyServer.newInstance(PUSH_SERVER_PORT)
                    .doOnStart(() -> System.out.println("push server start!"))
                    .doOnOpen((client, handshake) -> {
                        System.out.println("--------new push connected ---------------------------");
                        System.out.println("pushId: " + client.getResourceDescriptor());
                        System.out.println("local ip:" + client.getLocalSocketAddress());
                        System.out.println("remote ip:" + client.getRemoteSocketAddress());
                        System.out.println("-------------------------------------------------------");
                        PushSession.acceptClient(client, handshake);
                    })
                    .doOnError((client, ex) -> {
                        System.out.println("----------push error -----------------------------------");
                        System.out.println("---------------------------------------------------------");
                        ex.printStackTrace();
                    })
                    .doOnClose((client, code, reason, remote) -> {
                        System.out.println("----------push close -----------------------------------");
                        System.out.println("pushId: " + client.getResourceDescriptor());
                        System.out.println("remote: " + remote);
                        System.out.println("reason: "+reason);
                        System.out.println("---------------------------------------------------------");
                        PushSession.removeClient(client);
                    });

            // 启动服务器
            pushServer.start();

            // 初始化控制器
            PushServerController.init(pushServer);
        });
    }

    //    开启这个服务专门处理客户端发送的聊天信息
    private static void createSendServer() {
        threadPool.execute(()->{
            sendServer = MyServer.newInstance(SEND_SERVER_PORT)
                    .doOnStart(() -> System.out.println("send server start!"))
                    .doOnOpen((client, handshake) -> {
                        System.out.println("one send open: " + handshake.getResourceDescriptor());
                        System.out.println("remote ip: " + client.getRemoteSocketAddress());
                    })
                    .doOnMessage((client, message) -> {
                        SendSession.handle(client, message);
                    })
                    .doOnError((conn, ex) -> {
                        ex.printStackTrace();
                    })
                    .doOnClose((conn, code, reason, remote) -> {
                        System.out.println("send: onClose:-->" + conn.getRemoteSocketAddress());
                        System.out.println("send: reason:-->" + reason);
                    });
            //启动服务
            sendServer.start();

            // 初始化 send server controller
            SendServerController.init(sendServer);

        });
    }

    //    开启这个服务专门处理通用的客户端请求
    private static void createCommonServer() {
        threadPool.execute(()->{
            commonServer = MyServer.newInstance(COMMON_SERVER_PORT)
                    .doOnStart(() -> System.out.println("send server start!"))
                    .doOnOpen((client, handshake) -> {
                        CommonSession.handle(commonServer,client,handshake);
                    })
                    .doOnMessage((client, message) -> {
                        System.out.println("message: "+message);
                        CommonSession.handle(message);
                    })
                    .doOnError((client, ex) -> {
                        System.out.println("onError:-->" + client.getRemoteSocketAddress());
                        ex.printStackTrace();
                    })
                    .doOnClose((client, code, reason, remote) -> {
                    });

            // 启动服务器
            commonServer.start();

            // 初始化common server controller
            CommonServerController.init();
        });
    }

    // 创建文件传输服务
    private static void createFileServer() {
        threadPool.execute(()->{
            fileServer = MyServer.newInstance(FILE_SERVER_PORT)
                    .doOnStart(() -> System.out.println("file server start!"))
                    .doOnOpen((client, handshake) -> {
                        System.out.println("-------------------------------------------------------");
                        System.out.println("file server open: "+client.getRemoteSocketAddress());
                        FileSession.handle(client, handshake);

                    })
                    .doOnMessage((client, message) -> {
                        FileSession.handle(client, message);
                    })
                    .doOnError((client, ex) -> {
                        System.out.println("onError:-->" + client.getRemoteSocketAddress());
                        ex.printStackTrace();
                    })
                    .doOnClose((client, code, reason, remote) -> {
                        System.out.println("file server close: " + client.getRemoteSocketAddress());
                        System.out.println("-------------------------------------------------------");
                    });

            // 启动服务器
            fileServer.start();

            // 初始化file server controller
            FileServerController.init(fileServer);
        });
    }

}
