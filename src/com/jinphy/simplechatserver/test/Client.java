package com.jinphy.simplechatserver.test;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class Client  extends WebSocketClient{
    public Client(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("client onOpen!");
        System.out.println("client-> connnected to the server "+getURI());
    }

    @Override
    public void onMessage(String message) {
        System.out.println("client-> got message: "+message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("client onClose!");
        System.out.println("client-> you have been disconnected from " + getURI() + ",Code:" + code + ",reason:" + reason);

    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    public static void main(String... args) throws InterruptedException {
        Client client = null;
        try {
            client = new Client(new URI("ws://0.0.0.0:4545/hello/world/user=jinphy&password=123"));
            client.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        int i = 0;
        while (true) {
            client.send("the client send message:" + (i++));

            Thread.sleep(1000);
        }
    }
}
