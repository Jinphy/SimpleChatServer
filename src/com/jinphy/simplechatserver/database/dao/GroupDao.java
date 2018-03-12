package com.jinphy.simplechatserver.database.dao;

import com.jinphy.simplechatserver.database.models.Result;
import com.jinphy.simplechatserver.database.operate.Database;
import com.jinphy.simplechatserver.models.db_models.Friend;
import com.jinphy.simplechatserver.models.db_models.Group;
import com.jinphy.simplechatserver.models.db_models.Member;
import com.jinphy.simplechatserver.models.db_models.Message;
import com.jinphy.simplechatserver.models.network_models.KeyValueArray;
import com.jinphy.simplechatserver.models.network_models.Response;
import com.jinphy.simplechatserver.network.RequestConfig;
import com.jinphy.simplechatserver.utils.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.jinphy.simplechatserver.constants.StringConst.SINGLE_QUOTATION_MARK;
import static com.jinphy.simplechatserver.models.network_models.Response.*;
import static com.jinphy.simplechatserver.utils.StringUtils.wrap;

/**
 * DESC:
 * Created by jinphy on 2018/3/10.
 */
public class GroupDao {

    private GroupDao() {

    }

    private static class InstanceHolder{
        static final GroupDao DEFAULT = new GroupDao();
    }

    public static GroupDao getInstance() {
        return InstanceHolder.DEFAULT;
    }

    /**
     * DESC: 执行创建群聊的整个过程
     *
     *
     * @param params 创建群聊的参数
     * @param members 群成员的对应的用户账号
     * Created by jinphy, on 2018/3/10, at 11:08
     */
    public synchronized boolean buildGroup(Map<String, String> params, String[] members) {
        if (members == null || members.length == 0) {
            return false;
        }

        // 启动事务
        return Database.execute(statement -> {
            String sql;

            // 创建群
            for (String member : members) {
                params.put(RequestConfig.Key.owner, member);
                KeyValueArray parse = KeyValueArray.parse(params);
                sql = Database.insert()
                        .tables(Database.TABLE_GROUP_CHAT)
                        .columnNames(parse.keys)
                        .columnValues(parse.values)
                        .generateSql();
                System.out.println("sql====> "+sql);
                statement.addBatch(sql);
            }

            // 创建成员
            MemberDao memberDao = MemberDao.getInstance();
            for (String member : members) {
                 sql = memberDao.generateInsertSql(
                        params.get(Member.GROUP_NO), member, Member.STATUS_OK);
                System.out.println("sql====> " + sql);
                statement.addBatch(sql);
            }

            // 判断群和成员是否创建成功
            int[] counts = statement.executeBatch();
            for (int count : counts) {
                if (count < 1) {
                    return false;
                }
            }
            return true;
        });
    }

    public Result get(String groupNo, String owner) {
        Database.Operate operate = Database.select()
                .tables(Database.TABLE_GROUP_CHAT)
                .columnNames(
                        Group.CREATOR,
                        Group.OWNER,
                        Group.NAME,
                        Group.GROUP_NO,
                        Group.MAX_COUNT,
                        Group.AUTO_ADD,
                        Group.SHOW_MEMBER_NAME,
                        Group.KEEP_SILENT,
                        Group.REJECT_MSG);
        if (groupNo != null && groupNo.length() > 0) {
            operate.whereEq(Group.GROUP_NO, groupNo);
        }

        return operate
                .whereEq(Group.OWNER, owner)
                .execute();
    }


    public Result search(String text) {
        String wrapedText = wrap(text, SINGLE_QUOTATION_MARK);
        return Database.select()
                .columnNames(
                        Group.CREATOR,
                        Group.OWNER,
                        Group.NAME,
                        Group.GROUP_NO,
                        Group.MAX_COUNT,
                        Group.AUTO_ADD,
                        Group.SHOW_MEMBER_NAME,
                        Group.KEEP_SILENT,
                        Group.REJECT_MSG)
                .tables(Database.TABLE_GROUP_CHAT)
                .where(Group.CREATOR + "=" + Group.OWNER
                        + " and ( "
                        + Group.GROUP_NO + "= " + wrapedText
                        + " or "
                        + Group.NAME + " = " + wrapedText
                        + " )")
                .execute();
    }

    public Result getAvatar(String groupNo) {
        return Database.select()
                .columnNames(Group.AVATAR)
                .tables(Database.TABLE_GROUP_CHAT)
                .whereEq(Group.GROUP_NO, groupNo)
                .execute();
    }

    public Result loadAvatars(String... groupNos) {
        return Database.select()
                .columnNames(Group.AVATAR, Group.GROUP_NO)
                .tables(Database.TABLE_GROUP_CHAT)
                .whereIn(Group.GROUP_NO, groupNos)
                .execute();
    }

    public synchronized Response modifyGroup(Map<String, String> params) {

        String groupNo = params.get(Group.GROUP_NO);
        String creator = params.get(Group.CREATOR);
        String owner = params.get(Group.OWNER);
        String name = params.get(Group.NAME);
        String maxCount = params.get(Group.MAX_COUNT);
        String autoAdd = params.get(Group.AUTO_ADD);
        String showMemberName = params.get(Group.SHOW_MEMBER_NAME);
        String keepSilent = params.get(Group.KEEP_SILENT);
        String rejectMsg = params.get(Group.REJECT_MSG);
        String avatar = params.get(Group.AVATAR);
        if (StringUtils.isEmpty(groupNo, owner)) {
            return Response.make(NO_PARAMS_MISSING, "参数不完整！", null);
        }

        // 保存数据库执行结果
        Response<Response> result = Response.make("", "", null);
        Map<String, String> tempMap = new LinkedHashMap<>();

        Database.execute(statement -> {
            String sql;
            boolean needNotify = false;

            // 更新群名（name）、群头像（avatar）
            if (!StringUtils.isEmpty(name)) {
                tempMap.put(Group.NAME, name);
            }
            if (!StringUtils.isEmpty(avatar)) {
                tempMap.put(Group.AVATAR, avatar);
            }
            if (tempMap.size() > 0) {
                KeyValueArray parse = KeyValueArray.parse(tempMap);
                sql = Database.update()
                        .tables(Database.TABLE_GROUP_CHAT)
                        .columnNames(parse.keys)
                        .columnValues(parse.values)
                        .whereEq(Group.GROUP_NO, groupNo)
                        .generateSql();
                statement.addBatch(sql);
                needNotify = true;
            }
            tempMap.clear();

            // 更新最大成员数（maxCount）、入群方式（autoAdd）
            if (!StringUtils.isEmpty(maxCount)) {
                tempMap.put(Group.MAX_COUNT, maxCount);
            }
            if (!StringUtils.isEmpty(autoAdd)) {
                tempMap.put(Group.AUTO_ADD, autoAdd);
            }
            if (tempMap.size() > 0) {
                if (StringUtils.isEmpty(creator)) {
                    result.setData(Response.make(NO_PARAMS_MISSING, "参数不完整！", null));
                    return false;
                } else if (!StringUtils.equal(creator, owner)) {
                    result.setData(Response.make(NO_PARAMS_ERROR, "群主才有权限修改！", null));
                    return false;
                } else {
                    KeyValueArray parse = KeyValueArray.parse(tempMap);
                    sql = Database.update()
                            .tables(Database.TABLE_GROUP_CHAT)
                            .columnNames(parse.keys)
                            .columnValues(parse.values)
                            .whereEq(Group.GROUP_NO, groupNo)
                            .generateSql();
                    statement.addBatch(sql);
                    needNotify = true;
                }
            }
            tempMap.clear();

            // 更新是否像是成员名（showMemberName）、消息免打扰（keepSilent）、屏蔽群消息（rejectMsg）
            if (!StringUtils.isEmpty(showMemberName)) {
                tempMap.put(Group.SHOW_MEMBER_NAME, showMemberName);
            }
            if (!StringUtils.isEmpty(keepSilent)) {
                tempMap.put(Group.KEEP_SILENT, keepSilent);
            }
            if (!StringUtils.isEmpty(rejectMsg)) {
                tempMap.put(Group.REJECT_MSG, rejectMsg);
            }
            if (tempMap.size() > 0) {
                KeyValueArray parse = KeyValueArray.parse(tempMap);
                sql = Database.update()
                        .tables(Database.TABLE_GROUP_CHAT)
                        .columnNames(parse.keys)
                        .columnValues(parse.values)
                        .whereEq(Group.GROUP_NO, groupNo)
                        .whereEq(Group.OWNER, owner)
                        .generateSql();
                statement.addBatch(sql);
            }

            // 执行更新
            int[] updated = statement.executeBatch();

            for (int i : updated) {
                if (i <= 0) {
                    result.setData(Response.make(NO_SERVER, "服务器异常，请稍后再试！", null));
                    return false;
                }
            }

            result.setData(Response.make(YES, "群聊更新成功！", null));

            if (needNotify) {
                List<String> members = MemberDao.getInstance().getMemberAccounts(groupNo, owner);
                Message message = new Message();
                message.setFromAccount(Friend.system);
                message.setContent("更新群聊：" + groupNo);
                message.setExtra(groupNo);
                message.setContentType(Message.TYPE_SYSTEM_RELOAD_GROUP);
                message.setCreateTime(System.currentTimeMillis() + "");
                for (String member : members) {
                    message.setToAccount(member);
                    MessageDao.getInstance().saveMessage(message);
                }
            }
            return true;
        });
        return result.getData();
    }

}
