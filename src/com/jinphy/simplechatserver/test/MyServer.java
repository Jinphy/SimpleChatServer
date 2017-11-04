package com.jinphy.simplechatserver.test;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Iterator;

public class MyServer extends WebSocketServer {

    public MyServer(int port) {
        super(new InetSocketAddress(port));
    }

    public MyServer(InetSocketAddress address) {
        super(address);
    }


    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        broadcast("new connection: "+handshake.getResourceDescriptor());
        Iterator<String> stringIterator = handshake.iterateHttpFields();
        while (stringIterator.hasNext()) {
            System.out.println(stringIterator.next());
        }

        System.out.println("server-> "+conn.getRemoteSocketAddress().getAddress().getHostAddress()+" connected the server!");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("server onClose!");
        broadcast(conn+"has disconnected the server!");
        System.out.println("server-> "+conn+"has disconnected the server!");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("server-> "+conn + ": " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();

    }

    @Override
    public void onStart() {
        System.out.println("server-> server started!");
        System.out.println("server-> the ip is "+getAddress().getHostName());
        System.out.println("server-> the port is "+getPort());
    }
}
