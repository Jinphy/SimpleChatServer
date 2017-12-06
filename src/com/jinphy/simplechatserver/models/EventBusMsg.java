package com.jinphy.simplechatserver.models;

import com.jinphy.simplechatserver.controller.MyServer;
import com.jinphy.simplechatserver.test.Client;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by jinphy on 2017/12/5.
 */
public class EventBusMsg {
    public String path;
    public Map<String,String> params;
    public MyServer server;
    public WebSocket client;
    public List<WebSocket> clients;

    public static EventBusMsg create(MyServer server, WebSocket client, ClientHandshake handshake) {
        return new EventBusMsg(server,client, handshake);
    }

    private EventBusMsg() {

    }
    private EventBusMsg(MyServer server, WebSocket client, ClientHandshake handshake) {
        this.client = client;
        this.clients = Arrays.asList(client);
        UrlObject url = UrlObject.parse(handshake.getResourceDescriptor());
        this.path = url.getPath();
        this.params = url.getParams();
    }
}
