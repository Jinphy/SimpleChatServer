package com.jinphy.simplechatserver.models.network_models;

import com.google.gson.reflect.TypeToken;
import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.GsonUtils;
import org.greenrobot.eventbus.EventBus;
import org.java_websocket.WebSocket;

import java.lang.reflect.Type;
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



    public String fileName;
    public long fileLength;
    public String taskId;
    public String status;
    public String taskType;
    public String content;
    public String url;

    public List<WebSocket> client;


    private static Type type = new TypeToken<Map<String, String>>() {
    }.getType();


    public static void handle(WebSocket client, String message) {
        Map<String, String> map = GsonUtils.toBean(EncryptUtils.decryptThenDecode(message), type);
        FileSession session = sessionMap.get(map.get("taskId"));
        if (session == null) {
            // 文件传输任务第一次执行
            session = new FileSession();
            session.taskId = map.get("taskId");
            session.taskType = map.get("taskType");
            session.client = Arrays.asList(client);
            if (UPLOAD.equalsIgnoreCase(session.taskType)) {
                // 上传文件
                session.fileName = map.get("fileName");
                session.fileLength = Long.valueOf(map.get("fileLength"));
                session.status = map.get("status");
            } else {
                // 下载文件
                session.url = map.get("url");
            }
            sessionMap.put(session.taskId, session);
        } else {
            // 文件传输任务不是第一次上传
            if (UPLOAD.equalsIgnoreCase(session.taskType)) {
                // 上传文件
                session.content = map.get("content");
                session.status = map.getOrDefault("status","doing");
            } else {
                // 下载文件
                // 下载文件客户端只请求一次，所以这种情况不存在
            }
        }
        EventBus.getDefault().post(session);
    }

}
