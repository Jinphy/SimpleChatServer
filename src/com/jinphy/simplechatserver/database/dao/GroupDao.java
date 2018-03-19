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
import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.GsonUtils;
import com.jinphy.simplechatserver.utils.StringUtils;
import com.sun.org.apache.regexp.internal.RE;

import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.jinphy.simplechatserver.constants.StringConst.FROM;
import static com.jinphy.simplechatserver.constants.StringConst.LINE;
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

    private static class InstanceHolder {
        static final GroupDao DEFAULT = new GroupDao();
    }

    public static GroupDao getInstance() {
        return InstanceHolder.DEFAULT;
    }

    /**
     * DESC: 执行创建群聊的整个过程
     *
     * @param group  创建群聊的参数
     * @param members 群成员的对应的用户账号
     *                Created by jinphy, on 2018/3/10, at 11:08
     */
    public synchronized boolean buildGroup(Map<String, String> group, String[] members) {
        if (members == null || members.length == 0) {
            return false;
        }

        // 启动事务
        return Database.execute(statement -> {
            String sql;

            // 第一步：创建群
            for (String member : members) {
                group.put(RequestConfig.Key.owner, member);
                KeyValueArray parse = KeyValueArray.parse(group);
                sql = Database.insert()
                        .tables(Database.TABLE_GROUP_CHAT)
                        .columnNames(parse.keys)
                        .columnValues(parse.values)
                        .generateSql();
                System.out.println("sql====> " + sql);
                statement.addBatch(sql);
            }

            // 第二步：创建成员
            MemberDao memberDao = MemberDao.getInstance();
            for (String member : members) {
                sql = memberDao.generateInsertSql(
                        group.get(Member.GROUP_NO), member, Member.STATUS_OK);
                System.out.println("sql====> " + sql);
                statement.addBatch(sql);
            }


            // 第三步：群和成员创建成功后在建立成员之间的好友关系（好友关系的状态是未验证的）
            FriendDao friendDao = FriendDao.getInstance();
            for (int i = 0; i < members.length-1; i++) {
                for (int j = i + 1; j < members.length; j++) {
                    buildMemberShip(statement, members[i], members[j]);
                }
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

    /**
     * DESC: 加入群聊
     *
     * @param account  申请加入群聊的账号
     * @param group 创建申请者对应的群的参数
     * Created by jinphy, on 2018/3/13, at 15:58
     */
    public synchronized Response joinGroup(Map<String, String> group, String account) {
        Response<Response> response = Response.make("", "", null);
        group.put(Group.OWNER, account);
        group.put(Group.SHOW_MEMBER_NAME, "true");
        group.put(Group.KEEP_SILENT, "false");
        group.put(Group.REJECT_MSG, "false");
        String groupNo = group.get(Group.GROUP_NO);

        Database.execute(statement -> {
            String sql;
            KeyValueArray parse = KeyValueArray.parse(group);
            sql = Database.insert()
                    .tables(Database.TABLE_GROUP_CHAT)
                    .columnNames(parse.keys)
                    .columnValues(parse.values)
                    .generateSql();
            statement.addBatch(sql);

            statement.addBatch(MemberDao.getInstance()
                    .generateInsertSql(groupNo, account, Member.STATUS_OK));

            int[] results = statement.executeBatch();
            for (int result : results) {
                if (result <= 0) {
                    response.setData(Response.make(NO_SERVER, "服务器异常，请稍后再试！", null));
                    return false;
                }
            }

            List<String> members = MemberDao.getInstance().getMemberAccounts(groupNo, account);
            FriendDao friendDao = FriendDao.getInstance();
            MessageDao messageDao = MessageDao.getInstance();
            Message message = new Message();
            message.setCreateTime(System.currentTimeMillis() + "");
            message.setFromAccount(Friend.system);
            message.setContentType(Message.TYPE_SYSTEM_NEW_MEMBER);
            message.setContent(groupNo);
            message.setExtra(GsonUtils.toJson(new String[]{account}));

            for (String member : members) {
                friendDao.addFriend(member, account);
                friendDao.addGroupCount(member, account, 1);
                friendDao.addFriend(account, member);
                friendDao.addGroupCount(account, member, 1);
                message.setToAccount(member);
                messageDao.saveMessage(message);
            }

            message.setContentType(Message.TYPE_SYSTEM_NEW_GROUP);
            message.setToAccount(account);

            members.add(account);
            String membersStr = GsonUtils.toJson(members);
            message.setExtra(groupNo + "@" + membersStr);

            message.setContent("您加入群聊：" + groupNo + " 的申请已通过，现在可以开始和大家聊天了！");
            MessageDao.getInstance().saveMessage(message);

            response.setData(Response.make(YES, "申请加入群聊成功！", null));
            return true;
        });

        return response.getData();
    }

    public boolean addMembers(Map<String, String> group, String operator, String... accounts) {
        group.put(Group.SHOW_MEMBER_NAME, "true");
        group.put(Group.KEEP_SILENT, "false");
        group.put(Group.REJECT_MSG, "false");

        return Database.execute(statement -> {
            String sql;
            KeyValueArray parse;
            // 第一步：创建群聊
            for (String account : accounts) {
                group.put(Group.OWNER, account);
                parse = KeyValueArray.parse(group);
                sql = Database.insert()
                        .tables(Database.TABLE_GROUP_CHAT)
                        .columnNames(parse.keys)
                        .columnValues(parse.values)
                        .generateSql();
                statement.addBatch(sql);
            }


            // 第二步：创建成员
            MemberDao memberDao = MemberDao.getInstance();
            for (String member : accounts) {
                sql = memberDao.generateInsertSql(group.get(Member.GROUP_NO), member, Member.STATUS_OK);
                statement.addBatch(sql);
            }

            // 第三步：群和成员创建成功后在建立成员之间的好友关系（好友关系的状态是未验证的）
            FriendDao friendDao = FriendDao.getInstance();
            // 建立新成员之间的关系
            for (int i = 0; i < accounts.length - 1; i++) {
                for (int j = i + 1; j < accounts.length; j++) {
                    buildMemberShip(statement, accounts[i], accounts[j]);
                }
            }
            // 建立新成员与旧成员之间的关系
            List<String> oldMembers = MemberDao.getInstance().getMemberAccounts(group.get(Group.GROUP_NO), accounts);
            for (String newMember : accounts) {
                for (String oldMember : oldMembers) {
                    buildMemberShip(statement, newMember, oldMember);
                }
            }


            // 第四步：判断是否操作成功
            int[] results = statement.executeBatch();
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }
            return true;
        });

    }

    /**
     * DESC: 建立两个群成员之间的关系
     * Created by jinphy, on 2018/3/18, at 10:00
     */
    private void buildMemberShip(Statement statement, String member1, String member2) throws SQLException {
        Result friend = FriendDao.getInstance().getFriend(member1, member2);
        String sql;
        int groupCount = 0;
        // 创建好友关系
        if (friend.count <= 0) {
            sql = Database.insert()
                    .tables(Database.TABLE_FRIEND)
                    .columnNames(Friend.ACCOUNT, Friend.OWNER, Friend.STATUS)
                    .columnValues(member1, member2, Friend.STATUS_WAITING)
                    .generateSql();
            statement.addBatch(sql);

            sql = Database.insert()
                    .tables(Database.TABLE_FRIEND)
                    .columnNames(Friend.ACCOUNT, Friend.OWNER, Friend.STATUS)
                    .columnValues(member2, member1, Friend.STATUS_WAITING)
                    .generateSql();
            statement.addBatch(sql);
        } else {
            String s = friend.first.get(Friend.GROUP_COUNT);
            if (s != null) {
                groupCount = Integer.valueOf(s);
            }
        }
        // 增加两个好友之间的共同群聊数
        groupCount++;
        sql = Database.update()
                .tables(Database.TABLE_FRIEND)
                .columnNames(Friend.GROUP_COUNT)
                .columnValues(groupCount)
                .whereEq(Friend.ACCOUNT, member1)
                .whereEq(Friend.OWNER, member2)
                .generateSql();
        statement.addBatch(sql);

        sql = Database.update()
                .tables(Database.TABLE_FRIEND)
                .columnNames(Friend.GROUP_COUNT)
                .columnValues(groupCount)
                .whereEq(Friend.ACCOUNT, member2)
                .whereEq(Friend.OWNER, member1)
                .generateSql();
        statement.addBatch(sql);
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

    public Result get(String groupNo) {
        return Database.select()
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
                        Group.REJECT_MSG)
                .whereEq(Group.GROUP_NO, groupNo)
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
                if (members == null || members.size() == 0) {
                    return true;
                }
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

    /**
     * DESC: 将成员从群众删除
     *
     * @param bySelf 表示是否是成员自动退出的，true表示自动退出，false表示群主删除
     *               Created by jinphy, on 2018/3/13, at 20:16
     */
    public synchronized boolean removeMember(String groupNo, String creator, String member, boolean bySelf) {
        return Database.execute(statement -> {
            String sql;

            // 第一步：获取该群众的所有成员（除了要退出该群的这个成员外）
            List<String> members = MemberDao.getInstance().getMemberAccounts(groupNo, member);

            if (members == null || members.size() <= 0) {
                return false;
            }

            // 第二步：将属于该成员的群删除
            sql = Database.delete()
                    .tables(Database.TABLE_GROUP_CHAT)
                    .whereEq(Group.GROUP_NO, groupNo)
                    .whereEq(Group.OWNER, member)
                    .generateSql();
            statement.addBatch(sql);

            // 第三步：删除该成员
            sql = Database.delete()
                    .tables(Database.TABLE_MEMBER)
                    .whereEq(Member.GROUP_NO, groupNo)
                    .whereEq(Member.ACCOUNT, member)
                    .generateSql();
            statement.addBatch(sql);

            // 第四步：将该成员和其他成员中的彼此好友中的群组数减1，表示他们共同的群数减1
            FriendDao friendDao = FriendDao.getInstance();
            Result friendResult;
            Map<String, String> friend;
            int groupCount;
            for (String m : members) {
                friendResult = friendDao.getFriend(member, m);
                if (friendResult.count > 0) {
                    friend = friendResult.first;
                    groupCount = Integer.valueOf(friend.get(Friend.GROUP_COUNT));
                    if (groupCount > 0) {
                        sql = Database.update()
                                .tables(Database.TABLE_FRIEND)
                                .columnNames(Friend.GROUP_COUNT)
                                .columnValues(groupCount - 1)
                                .whereEq(Friend.ACCOUNT, m)
                                .whereEq(Friend.OWNER, member)
                                .generateSql();
                        statement.addBatch(sql);

                        sql = Database.update()
                                .tables(Database.TABLE_FRIEND)
                                .columnNames(Friend.GROUP_COUNT)
                                .columnValues(groupCount - 1)
                                .whereEq(Friend.ACCOUNT, member)
                                .whereEq(Friend.OWNER, m)
                                .generateSql();
                        statement.addBatch(sql);

                    }
                    if (groupCount == 1) {
                        String status = friend.get(Friend.STATUS);
                        switch (status) {
                            case Friend.STATUS_OK:
                            case Friend.STATUS_BLACK_LISTING:
                            case Friend.STATUS_BLACK_LISTED:
                                break;
                            default:
                                sql = Database.delete()
                                        .tables(Database.TABLE_FRIEND)
                                        .whereEq(Friend.ACCOUNT, m)
                                        .whereEq(Friend.OWNER, member)
                                        .generateSql();
                                statement.addBatch(sql);

                                sql = Database.delete()
                                        .tables(Database.TABLE_FRIEND)
                                        .whereEq(Friend.ACCOUNT, member)
                                        .whereEq(Friend.OWNER, m)
                                        .generateSql();
                                statement.addBatch(sql);
                        }
                    }
                }
            }
            // 第五步：批执行以上的操作
            int[] results = statement.executeBatch();
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }

            // 第六步：通知其他成员更新该删除结果
            if (!bySelf) {
                members.add(member);
                for (int i = 0; i < members.size(); i++) {
                    if (StringUtils.equal(members.get(i), creator)) {
                        members.remove(i);
                        break;
                    }
                }
            }
            MessageDao messageDao = MessageDao.getInstance();
            Message message = new Message();
            message.setFromAccount(Friend.system);
            message.setCreateTime(System.currentTimeMillis() + "");
            message.setContentType(Message.TYPE_SYSTEM_DELETE_MEMBER);
            message.setContent(bySelf ?new String(member + "已退出群聊！")  :new String( member + "已被群主移出群聊！"));
            message.setExtra(groupNo + "@" + member);

            for (String m : members) {
                message.setToAccount(m);
                messageDao.saveMessage(message);
            }
            return true;
        });
    }

    /**
     * DESC: 解除群聊
     * Created by jinphy, on 2018/3/13, at 20:17
     */
    public synchronized boolean breakGroup(String groupNo, String creator) {
        return Database.execute(statement -> {
            String sql;

            // 第一步：获取该群众的所有成员
            List<String> members = MemberDao.getInstance().getMemberAccounts(groupNo);

            if (members == null || members.size() <= 0) {
                return false;
            }

            // 第二步：删除群
            sql = Database.delete()
                    .tables(Database.TABLE_GROUP_CHAT)
                    .whereEq(Group.GROUP_NO, groupNo)
                    .generateSql();
            statement.addBatch(sql);

            // 第三步：删除成员
            sql = Database.delete()
                    .tables(Database.TABLE_MEMBER)
                    .whereEq(Member.GROUP_NO, groupNo)
                    .generateSql();
            statement.addBatch(sql);

            // 第四步：将成员之间的彼此好友中的群组数减1，表示他们共同的群数减1
            FriendDao friendDao = FriendDao.getInstance();
            Result friendResult;
            Map<String, String> friend;
            int groupCount;
            int size = members.size();
            String memberA;
            String memberB;
            for (int i = 0; i < size - 1; i++) {
                memberA = members.get(i);
                for (int j = i + 1; j < size; j++) {
                    memberB = members.get(j);
                    friendResult = friendDao.getFriend(memberA, memberB);
                    if (friendResult.count > 0) {
                        friend = friendResult.first;
                        groupCount = Integer.valueOf(friend.get(Friend.GROUP_COUNT));
                        if (groupCount > 0) {
                            sql = Database.update()
                                    .tables(Database.TABLE_FRIEND)
                                    .columnNames(Friend.GROUP_COUNT)
                                    .columnValues(groupCount - 1)
                                    .whereEq(Friend.ACCOUNT, memberB)
                                    .whereEq(Friend.OWNER, memberA)
                                    .generateSql();
                            statement.addBatch(sql);

                            sql = Database.update()
                                    .tables(Database.TABLE_FRIEND)
                                    .columnNames(Friend.GROUP_COUNT)
                                    .columnValues(groupCount - 1)
                                    .whereEq(Friend.ACCOUNT, memberA)
                                    .whereEq(Friend.OWNER, memberB)
                                    .generateSql();
                            statement.addBatch(sql);

                        }
                        if (groupCount == 1) {
                            String status = friend.get(Friend.STATUS);
                            switch (status) {
                                case Friend.STATUS_OK:
                                case Friend.STATUS_BLACK_LISTING:
                                case Friend.STATUS_BLACK_LISTED:
                                    break;
                                default:
                                    sql = Database.delete()
                                            .tables(Database.TABLE_FRIEND)
                                            .whereEq(Friend.ACCOUNT, memberB)
                                            .whereEq(Friend.OWNER, memberA)
                                            .generateSql();
                                    statement.addBatch(sql);

                                    sql = Database.delete()
                                            .tables(Database.TABLE_FRIEND)
                                            .whereEq(Friend.ACCOUNT, memberA)
                                            .whereEq(Friend.OWNER, memberB)
                                            .generateSql();
                                    statement.addBatch(sql);
                            }
                        }
                    }
                }
            }

            // 第五步：批执行以上的操作
            int[] results = statement.executeBatch();
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }

            // 第六步：通知其他成员更新该删除结果
            MessageDao messageDao = MessageDao.getInstance();
            Message message = new Message();
            message.setFromAccount(Friend.system);
            message.setCreateTime(System.currentTimeMillis() + "");
            message.setContentType(Message.TYPE_SYSTEM_BREAK_GROUP);
            message.setContent(new String("已被群主解散！"));
            message.setExtra(groupNo);

            for (String m : members) {
                if (StringUtils.noEqual(m, creator)) {
                    message.setToAccount(m);
                    messageDao.saveMessage(message);
                }
            }
            return true;
        });
    }

    /**
     * DESC: 获取可以就收群消息的成员
     *
     * @param groupNo 群号
     * @param msgSender 发送消息的成员
     * Created by jinphy, on 2018/3/18, at 20:55
     */
    public List<String> getCanReceiveMsgMembers(String groupNo, String msgSender) {
        Result result = Database.select()
                .columnNames(Group.OWNER)
                .tables(Database.TABLE_GROUP_CHAT)
                .whereEq(Group.GROUP_NO, groupNo)
                .whereEq(Group.REJECT_MSG, "false")
                .whereLB(Group.OWNER, msgSender)
                .execute();
        List<String> members = new LinkedList<>();
        if (result.count > 0) {
            for (Map<String, String> member : result.data) {
                members.add(member.get(Group.OWNER));
            }
        }
        return members;
    }
}
