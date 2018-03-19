package com.jinphy.simplechatserver.database.dao;

import com.jinphy.simplechatserver.database.models.Result;
import com.jinphy.simplechatserver.database.operate.Database;
import com.jinphy.simplechatserver.models.db_models.Message;
import com.jinphy.simplechatserver.models.network_models.PushSession;
import com.jinphy.simplechatserver.utils.EncryptUtils;
import sun.nio.cs.ext.MS874;

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
        msg.setContent(EncryptUtils.encode(msg.getContent()));

        String toAccount = msg.getToAccount();

        if (toAccount.contains("G")) {
            // 群聊消息
            boolean result = Database.execute(statement->{
                List<String> members = GroupDao.getInstance().getCanReceiveMsgMembers(toAccount, msg.getFromAccount());
                if (members != null && members.size() > 0) {
                    msg.setExtra(null);
                    msg.extra(Message.KEY_GROUP_NO, toAccount);
                    msg.extra(Message.KEY_CHAT_GROUP, "true");
                    for (String member : members) {
                        String sql = Database.insert()
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
                                        member,
                                        msg.getContent(),
                                        msg.getContentType(),
                                        msg.getCreateTime(),
                                        msg.getExtra()
                                )
                                .generateSql();
                        statement.addBatch(sql);
                    }
                    int[] results = statement.executeBatch();
                    for (int r : results) {
                        if (r <= 0) {
                            return false;
                        }
                    }
                    for (String member : members) {
                        PushSession.pushMessage(member);
                    }
                    return true;
                }
                return true;
            });
            if (result) {
                return Result.ok(1, null, null);
            } else {
                return Result.error();
            }
        } else {
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
            for (Map<String, String> msg : result.data) {
                String decodedContent = EncryptUtils.decode(msg.get(Message.CONTENT));
                msg.put(Message.CONTENT, decodedContent);
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
