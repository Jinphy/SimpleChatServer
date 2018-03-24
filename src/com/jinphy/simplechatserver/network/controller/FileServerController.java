package com.jinphy.simplechatserver.network.controller;

import com.jinphy.simplechatserver.models.network_models.FileSession;
import com.jinphy.simplechatserver.network.MyServer;
import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.GsonUtils;
import com.jinphy.simplechatserver.utils.StringUtils;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.OpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DESC:
 * Created by jinphy on 2018/1/19.
 */
public class FileServerController extends BaseController {

    MyServer fileServer;

//    Map<String, UploadData> uploadMap = new ConcurrentHashMap<>();

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
        if (session.task.isUpload) {
            handleUpload(session);
        } else {
            handleDownload(session);
        }

    }

    /**
     * DESC: 处理下载的任务
     * Created by jinphy, on 2018/1/19, at 18:06
     */
    private void handleDownload(FileSession session) {
        BufferedSource source = session.task.source;
        if (source == null) {
            fileServer.broadcast("错误：文件不存在", session.client);
            return;
        }
        Map<String, String> map = new HashMap<>();
        int readCount;
        byte[] buffer = new byte[102400];
        // 记录发送的顺序
        int times = 0;

        System.out.println("download file start: " + session.task.fileName);
        try {
            while ((readCount = source.read(buffer)) != -1) {
                map.put("content", StringUtils.bytesToStr(buffer, 0, readCount));
                map.put("times", (++times)+"");
                fileServer.broadcast(GsonUtils.toJson(map), session.client);
                System.out.println("times: " + times);
            }
            map.put("content", "[end]");
            fileServer.broadcast(GsonUtils.toJson(map), session.client);
        } catch (Exception e) {
            e.printStackTrace();
            fileServer.broadcast("error", session.client);
        }finally {
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
        FileTask task = session.task;
        // 已经上传完毕
        try {
            if (task.sink == null) {
                // 文件创建失败
                session.client.get(0).close();
                FileSession.sessionMap.remove(session.address);
                return;
            }
            byte[] buffer;
            while ((buffer = session.getBuffer()) != null) {
                task.write(buffer);
                if (session.isFinished()) {
                    break;
                }
            }
            System.out.println("upload ok");

            // 上传完毕
            fileServer.broadcast("ok", session.client);

            task.sink.flush();
            task.sink.close();

            FileSession.sessionMap.remove(session.address);
        } catch (IOException e) {
            e.printStackTrace();
            fileServer.broadcast("error", session.client);
        }
    }
    public static class FileTask{

        public String fileName;
        public boolean isUpload;
        public BufferedSink sink;
        public BufferedSource source;

        public static FileTask create(String fileName,boolean isUpload) {
            return new FileTask(fileName,isUpload);
        }

        private FileTask(String fileName, boolean isUpload) {
            this.isUpload = isUpload;
            this.fileName = fileName;
            File file = new File("./files", fileName);
            if (isUpload) {
                // 上传任务
                file.getParentFile().mkdirs();
                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    sink = Okio.buffer(Okio.sink(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // 下载任务
                if (!file.exists()) {
                    return;
                }
                try {
                    source = Okio.buffer(Okio.source(file));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        public synchronized void write(byte[] buffer) {
            try {
                this.sink.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
