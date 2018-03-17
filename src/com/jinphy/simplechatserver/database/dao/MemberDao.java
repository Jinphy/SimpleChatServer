package com.jinphy.simplechatserver.database.dao;

import com.jinphy.simplechatserver.database.models.Result;
import com.jinphy.simplechatserver.database.operate.Database;
import com.jinphy.simplechatserver.models.db_models.Friend;
import com.jinphy.simplechatserver.models.db_models.Member;
import com.jinphy.simplechatserver.models.db_models.Message;
import com.jinphy.simplechatserver.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * DESC:
 * Created by jinphy on 2018/3/10.
 */
public class MemberDao {


    private MemberDao() {

    }

    private static class InstanceHolder{
        static final MemberDao DEFAULT = new MemberDao();
    }

    public static MemberDao getInstance() {
        return InstanceHolder.DEFAULT;
    }

    public String generateInsertSql(String groupNo, String account,String status) {
        return Database.insert()
                .tables(Database.TABLE_MEMBER)
                .columnNames(Member.GROUP_NO, Member.ACCOUNT, Member.ALLOW_CHAT, Member.STATUS)
                .columnValues(groupNo, account, true, status)
                .generateSql();
    }

    public Result saveMember(String groupNo, String account, String status) {
        return Database.insert()
                .tables(Database.TABLE_MEMBER)
                .columnNames(Member.GROUP_NO, Member.ACCOUNT, Member.ALLOW_CHAT, Member.STATUS)
                .columnValues(groupNo, account, true, status)
                .execute();
    }


    public Result setStatus(String groupNo, String account, String status) {
        if (groupNo == null || account == null || status == null) {
            return Result.error();
        }
        return Database.update()
                .tables(Database.TABLE_MEMBER)
                .columnNames(Member.STATUS)
                .columnValues(status)
                .execute();
    }

    public List<String> getMemberAccounts(String groupNo) {
        Result result = Database.select()
                .columnNames(Member.ACCOUNT)
                .tables(Database.TABLE_MEMBER)
                .whereEq(Member.GROUP_NO, groupNo)
                .execute();
        LinkedList<String> members = new LinkedList<String>();
        if (result.count > 0) {
            for (Map<String, String> member : result.data) {
                members.add(member.get(Member.ACCOUNT));
            }
        }
        return members;
    }

    /**
     * DESC: 查找指定群聊的所有成员的账号，除了指定的账号外
     *
     *
     * @param groupNo 指定的群聊
     * @param exceptAccount 需要排除的账号
     * Created by jinphy, on 2018/3/12, at 14:11
     */
    public List<String> getMemberAccounts(String groupNo, String... exceptAccount) {
        if (StringUtils.isEmpty(groupNo) || exceptAccount==null || exceptAccount.length==0) {
            return null;
        }
        Result result = Database.select()
                .columnNames(Member.ACCOUNT)
                .tables(Database.TABLE_MEMBER)
                .whereEq(Member.GROUP_NO, groupNo)
                .whereNotIn(Member.ACCOUNT, exceptAccount)
                .execute();
        if (result.count <= 0) {
            return null;
        }
        LinkedList<String> out = new LinkedList<>();
        for (Map<String, String> map : result.data) {
            out.add(map.get(Member.ACCOUNT));
        }
        return out;
    }

    public Result getMembers(String... groupNos) {
        return Database.select()
                .columnNames(Member.GROUP_NO, Member.ACCOUNT, Member.ALLOW_CHAT, Member.STATUS)
                .tables(Database.TABLE_MEMBER)
                .whereIn(Member.GROUP_NO, groupNos)
                .execute();
    }

    public boolean modifyAllowChat(String groupNo, String account, String allowChat, String creator) {
        // 第一步：获取除了群主外的所有该群中的成员账号，以为只有群主才有这个权限修改该值
        List<String> members = getMemberAccounts(groupNo, creator);
        if (members == null || members.size() <= 0) {
            return false;
        }

        // 更新指定成员的是否允许发言
        Result result = Database.update()
                .tables(Database.TABLE_MEMBER)
                .columnNames(Member.ALLOW_CHAT)
                .columnValues(allowChat)
                .whereEq(Member.GROUP_NO, groupNo)
                .whereIn(Member.ACCOUNT, account)
                .execute();
        if (result.count <= 0) {
            return false;
        }


        // 通知成员更新变更
        MessageDao messageDao = MessageDao.getInstance();
        Message message = new Message();
        message.setFromAccount(Friend.system);
        message.setCreateTime(System.currentTimeMillis() + "");
        message.setContentType(Message.TYPE_SYSTEM_MEMBER_ALLOW_CHAT);
        message.setContent(new String("已被群主禁言！"));
        message.setExtra(groupNo + "@" + account);

        for (String member : members) {
            message.setToAccount(member);
            messageDao.saveMessage(message);
        }
        return true;
    }

    public Result getMember(String groupNo, String account) {
        return Database.select()
                .columnNames(Member.GROUP_NO, Member.GROUP_NO, Member.ALLOW_CHAT, Member.STATUS)
                .tables(Database.TABLE_MEMBER)
                .whereEq(Member.GROUP_NO, groupNo)
                .whereEq(Member.ACCOUNT, account)
                .whereEq(Member.STATUS, Member.STATUS_OK)
                .execute();
    }

}
