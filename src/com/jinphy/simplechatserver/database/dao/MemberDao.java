package com.jinphy.simplechatserver.database.dao;

import com.jinphy.simplechatserver.database.models.Result;
import com.jinphy.simplechatserver.database.operate.Database;
import com.jinphy.simplechatserver.models.db_models.Member;

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

    public List<String> getMembersAccount(String groupNo) {
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

    public Result getMembers(String... groupNos) {
        return Database.select()
                .columnNames(Member.GROUP_NO, Member.ACCOUNT, Member.ALLOW_CHAT, Member.STATUS)
                .tables(Database.TABLE_MEMBER)
                .whereIn(Member.GROUP_NO, groupNos)
                .execute();
    }
}
