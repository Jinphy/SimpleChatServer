package com.jinphy.simplechatserver.database.dao;

import com.jinphy.simplechatserver.database.models.Result;
import com.jinphy.simplechatserver.database.operate.Database;
import com.jinphy.simplechatserver.models.db_models.Message;
import com.jinphy.simplechatserver.models.network_models.PushSession;

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
        Result result = Database.insert()
                .tables(Database.TABLE_MESSAGE)
                .columnNames(
                        FROM,
                        TO,
                        CONTENT,
                        CONTENT_TYPE,
                        CREATE_TIME
                )
                .columnValues(
                        msg.getFromAccount(),
                        msg.getToAccount(),
                        msg.getContent(),
                        msg.getContentType(),
                        msg.getCreateTime()
                )
                .execute();

        // 通知推送服务推送新消息
        PushSession.pushMessage(msg.getToAccount());
        return result;
    }

    public Result loadMessage(String to) {
        return Database.select()
                .tables(Database.TABLE_MESSAGE)
                .whereEq(TO, to)
                .whereEq(IS_NEW, true)
                .execute();
    }

    /**
     * DESC: 更新消息
     * Created by jinphy, on 2018/2/27, at 14:17
     */
    public void updateMessage(List<Map<String, String>> messages) {
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
