package com.jinphy.simplechatserver.database.dao;

import com.jinphy.simplechatserver.constants.StringConst;
import com.jinphy.simplechatserver.database.models.Result;
import com.jinphy.simplechatserver.database.operate.Database;
import com.jinphy.simplechatserver.models.db_models.Group;
import com.jinphy.simplechatserver.models.db_models.Member;
import com.jinphy.simplechatserver.models.network_models.KeyValueArray;
import com.jinphy.simplechatserver.network.RequestConfig;
import com.jinphy.simplechatserver.utils.ObjectHelper;
import com.jinphy.simplechatserver.utils.StringUtils;

import java.sql.Statement;
import java.util.Map;

import static com.jinphy.simplechatserver.constants.StringConst.SINGLE_QUOTATION_MARK;
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

}
