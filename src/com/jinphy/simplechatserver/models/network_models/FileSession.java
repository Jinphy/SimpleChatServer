package com.jinphy.simplechatserver.models.network_models;

import com.google.gson.reflect.TypeToken;
import com.jinphy.simplechatserver.network.controller.FileServerController;
import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.GsonUtils;
import com.jinphy.simplechatserver.utils.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.Handshakedata;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DESC:
 * Created by jinphy on 2018/1/19.
 */
public class FileSession {

    public static final Map<String, FileSession> sessionMap = new ConcurrentHashMap<>();

    public static final String DOWNLOAD = "download";
    public static final String UPLOAD = "upload";

    public static final String START = "start";
    public static final String END = "end";
    public static final String DOING = "doing";

//    public String fileName;
//    public long fileLength;
//    public String taskId;
//    public String status;
//    public String taskType;
//    public String content;
//    public String url;


    public String address;
    public List<WebSocket> client;
    public FileServerController.FileTask task;
    public int totalTimes;

    /**
     * DESC: 当前写入文件的次数
     * Created by jinphy, on 2018/3/22, at 9:46
     */
    private int writeTimes;



    public Map<Integer, byte[]> bufferMap = new ConcurrentHashMap<>();

    private static Type type = new TypeToken<Map<String, String>>() {
    }.getType();


    public static void handle(WebSocket client, String message) {
        String address = client.getRemoteSocketAddress().toString();
        FileSession session = sessionMap.get(address);
        if (session != null) {
            Map<String, String> map = GsonUtils.toBean(message, type);
            String content = map.get("content");
            Integer times = Integer.valueOf(map.get("times"));
            if ("[end]".equals(content)) {
                // 这种情况是上传文件的
                session.totalTimes = times;
            } else if ("error".equals(content)) {
                // 客户端异常
                sessionMap.remove(session.address);
                session.client.get(0).close();
                if (session.task.source != null) {
                    try {
                        session.task.source.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                session.putBuffer(times, StringUtils.strToBytes(content));
            }
        }
    }

    public void putBuffer(int times, byte[] buffer) {
        bufferMap.put(times, buffer);
    }


    public synchronized byte[] getBuffer() {
        if (totalTimes == 0 || writeTimes < totalTimes) {
            writeTimes++;
            byte[] buffer;
            while ((buffer = bufferMap.remove(writeTimes)) == null) {
                if (isFinished()) {
                    break;
                }
                continue;
            }
            System.out.println("times: " + writeTimes);
            return buffer;
        } else {
            return null;
        }
    }

    public boolean isFinished() {
        return totalTimes > 0 && writeTimes >= totalTimes;
    }



    public static void handle(WebSocket client, Handshakedata handshake) {
        InetSocketAddress address = client.getRemoteSocketAddress();
        FileSession fileSession = new FileSession();
        fileSession.client = Arrays.asList(client);
        fileSession.address = address.toString();
        String fileName = handshake.getFieldValue("FILE_NAME");
        boolean isUplad = StringUtils.equal("upload", handshake.getFieldValue("TASK_TYPE"));
        fileSession.task = FileServerController.FileTask.create(fileName, isUplad);
        sessionMap.put(fileSession.address, fileSession);
        EventBus.getDefault().post(fileSession);
    }

}
