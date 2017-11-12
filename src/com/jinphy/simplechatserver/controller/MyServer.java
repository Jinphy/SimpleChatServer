package com.jinphy.simplechatserver.controller;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class MyServer extends WebSocketServer {

    protected OnStart onStart;
    protected OnOpen onOpen;
    protected OnMessage onMessage;
    protected OnError onError;
    protected OnClose onClose;

    public static MyServer newInstance(int port) {
        return new MyServer(port);
    }

    private MyServer(int port) {
        this(new InetSocketAddress(port));
    }

    private MyServer(InetSocketAddress address) {
        super(address);
    }

    //----------------设置各个回调接口---------------------------------------
    public MyServer doOnStart(OnStart onStart) {
        this.onStart = onStart;
        return this;
    }

    public MyServer doOnOpen(OnOpen onOpen) {
        this.onOpen = onOpen;
        return this;
    }

    public MyServer doOnMessage(OnMessage onMessage) {
        this.onMessage = onMessage;
        return this;
    }

    public MyServer doOnError(OnError onError) {
        this.onError = onError;
        return this;
    }

    public MyServer doOnClose(OnClose onClose) {
        this.onClose = onClose;
        return this;
    }

    //------------------重写接口WebSocketServer抽象方法------------------------------

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (this.onOpen != null) {
            this.onOpen.onOpen(conn,handshake);
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (this.onClose != null) {
            this.onClose.onClose(conn, code, reason, remote);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (this.onMessage != null) {
            this.onMessage.onMessage(conn, message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        if (this.onError != null) {
            this.onError.onError(conn, ex);
        }
    }

    @Override
    public void onStart() {
        if (this.onStart != null) {
            this.onStart.onStart();
        }
    }


    //----------interface---------------------------------------------------
//    开始接口，在服务器开始时调用
    public interface OnStart{
        void onStart();
    }
//    连接打开接口，在打开与某个客户端的连接时调用
    public interface OnOpen{
        void onOpen(WebSocket conn, ClientHandshake handshake);
    }
//    消息接口，在收到来自某个客户端的消息是调用
    public interface OnMessage{
        void onMessage(WebSocket conn, String message);
    }
//    错误接口，在与某个客户端的连接发生错误时调用
    public interface OnError{
        void onError(WebSocket conn, Exception ex);
    }
//    关闭接口，在与某个客户端的连接关闭时调用
    public interface OnClose{
        void onClose(WebSocket conn, int code, String reason, boolean remote);
    }
}
