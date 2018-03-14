package com.jinphy.simplechatserver.database.dao;

import com.jinphy.simplechatserver.constants.StringConst;
import com.jinphy.simplechatserver.database.models.Result;
import com.jinphy.simplechatserver.database.operate.Database;
import com.jinphy.simplechatserver.models.db_models.Message;
import com.jinphy.simplechatserver.models.network_models.PushSession;
import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import static com.jinphy.simplechatserver.models.db_models.Message.*;

/**
 * DESC:
 * Created by jinphy on 2018/1/16.
 */
public class MessageDao {

    private static class InstanceHolder{
        static final MessageDao DEFAULT = new MessageDao();
    }

    public static MessageDao getInstance() {
        return InstanceHolder.DEFAULT;
    }

    private MessageDao(){}

    /**
     * DESC: 保存新消息
     * Created by jinphy, on 2018/2/27, at 14:03
     */
    public synchronized Result saveMessage(Message msg) {
        try {
            msg.setContent(URLEncoder.encode(msg.getContent(), StringConst.UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Result result = Database.insert()
                .tables(Database.TABLE_MESSAGE)
                .columnNames(
                        FROM,
                        TO,
                        CONTENT,
                        CONTENT_TYPE,
                        CREATE_TIME,
                        EXTRA
                )
                .columnValues(
                        msg.getFromAccount(),
                        msg.getToAccount(),
                        msg.getContent(),
                        msg.getContentType(),
                        msg.getCreateTime(),
                        msg.getExtra()
                )
                .execute();

        // 通知推送服务推送新消息
        PushSession.pushMessage(msg.getToAccount());
        return result;
    }

    public synchronized Result loadMessage(String to) {
        Result result = Database.select()
                .columnNames(
                        Message.ID,
                        Message.FROM,
                        Message.TO,
                        Message.CREATE_TIME,
                        Message.CONTENT,
                        Message.CONTENT_TYPE,
                        Message.EXTRA)
                .tables(Database.TABLE_MESSAGE)
                .whereEq(TO, to)
                .whereEq(IS_NEW, true)
                .execute();
        if (result.count > 0) {
            try {
                for (Map<String, String> msg : result.data) {
                    String decodedContent = URLDecoder.decode(msg.get(Message.CONTENT), StringConst.UTF_8);
                    msg.put(Message.CONTENT, decodedContent);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * DESC: 更新消息
     * Created by jinphy, on 2018/2/27, at 14:17
     */
    public synchronized void updateMessage(List<Map<String, String>> messages) {
        if (messages == null || messages.size() == 0) {
            return;
        }
        String[] ids = new String[messages.size()];
        int i = 0;
        for (Map<String, String> message : messages) {
            ids[i++] = message.get(Message.ID);
        }
        Database.update()
                .tables(Database.TABLE_MESSAGE)
                .columnNames(Message.IS_NEW)
                .columnValues(false)
                .whereIn(Message.ID, ids)
                .execute();
    }
}
