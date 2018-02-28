package com.jinphy.simplechatserver.network.controller;

import com.jinphy.simplechatserver.models.network_models.FileSession;
import com.jinphy.simplechatserver.network.MyServer;
import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.GsonUtils;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DESC:
 * Created by jinphy on 2018/1/19.
 */
public class FileServerController extends BaseController {

    MyServer fileServer;

    Map<String, UploadData> uploadMap = new ConcurrentHashMap<>();

    Map<String, BufferedSource> sourceMap = new ConcurrentHashMap<>();



    private static class InstanceHolder{
        static final FileServerController DEFAULT = new FileServerController();
    }

    public static FileServerController getInstance(MyServer fileServer) {
        InstanceHolder.DEFAULT.fileServer = fileServer;
        return InstanceHolder.DEFAULT;
    }

    public static void init(MyServer fileServer) {
        getInstance(fileServer);
    }

    private FileServerController() {
        EventBus.getDefault().register(this);
    }


    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void handleSession(FileSession session) {
        switch (session.taskType) {
            case FileSession.DOWNLOAD:
                handleDownload(session);
                break;
            case FileSession.UPLOAD:
                handleUpload(session);
                break;
            default:
                break;
        }
    }

    /**
     * DESC: 处理下载的任务
     * Created by jinphy, on 2018/1/19, at 18:06
     */
    private void handleDownload(FileSession session) {
        BufferedSource source = null;
        Map<String, Object> response = new HashMap<>();
        try {
            String fileName = session.url.substring(session.url.lastIndexOf("/") + 1);
            File file = new File("./files", fileName);
            long fileLength = file.length();
            source = Okio.buffer(Okio.source(file));

            response.put("fileName", fileName);
            response.put("fileLength", fileLength);
            fileServer.broadcast(EncryptUtils.encodeThenEncrypt(GsonUtils.toJson(response)), session.client);
            response.clear();

            byte[] buffer = new byte[102400];
            while (source.read(buffer) != -1) {
                response.put("content", new String(buffer, "utf8"));
                fileServer.broadcast(EncryptUtils.encodeThenEncrypt(GsonUtils.toJson(response)), session.client);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.clear();
            response.put("ok", false);
            fileServer.broadcast(EncryptUtils.encodeThenEncrypt(GsonUtils.toJson(response)), session.client);
        }finally {
            FileSession.sessionMap.remove(session.taskId);
            if (source != null) {
                try {
                    source.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * DESC: 处理上传的任务
     * Created by jinphy, on 2018/1/19, at 18:06
     */
    private void handleUpload(FileSession session) {
        switch (session.status) {
            case FileSession.START:
                UploadData upload = UploadData.parse(session);
                uploadMap.put(session.taskId, upload);
                break;
            case FileSession.DOING:
                try {
                    UploadData uploadData = uploadMap.get(session.taskId);
                    byte[] content = session.content.getBytes("utf8");
                    uploadData.update(content.length);
                    uploadData.sink.write(content);

                    Map<String, Object> response = new HashMap<>();
                    response.put("percentage", uploadData.percentage);
                    response.put("total", uploadData.totalLength);
                    response.put("current", uploadData.currentLength);
                    if (uploadData.currentLength == uploadData.totalLength) {
                        response.put("url", uploadData.url);
                    }
                    fileServer.broadcast(EncryptUtils.encodeThenEncrypt(GsonUtils.toJson(response)),session.client);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case FileSession.END:
                UploadData uploadData = uploadMap.remove(session.taskId);
                try {
                    uploadData.sink.flush();
                    uploadData.sink.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FileSession.sessionMap.remove(session.taskId);
                break;
            default:
                break;
        }
    }


    public static class UploadData{
        BufferedSink sink;
        long currentLength;
        long totalLength;
        float percentage;
        String url;

        public static UploadData parse(FileSession session) {
            UploadData uploadData = new UploadData();
            try {
                uploadData.totalLength = session.fileLength;
                File file = new File("./files", session.fileName);
                file.getParentFile().mkdirs();
                file.createNewFile();

                uploadData.sink = Okio.buffer(Okio.sink(file));
                uploadData.url = "ws:simplechat/files/" + session.fileName;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return uploadData;
        }

        public void update(long length) {
            currentLength += length;
            percentage = (float) (currentLength * 1.0 / totalLength);
        }

    }

}
