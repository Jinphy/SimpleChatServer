package com.jinphy.simplechatserver.database.dao;

import com.jinphy.simplechatserver.database.models.Result;
import com.jinphy.simplechatserver.database.operate.Database;
import com.jinphy.simplechatserver.models.db_models.Friend;
import com.jinphy.simplechatserver.models.db_models.User;

import java.util.Map;

/**
 * DESC:
 * Created by jinphy on 2018/2/27.
 */
public class FriendDao {

    private static class InstanceHolder{
        static FriendDao DEFAULT = new FriendDao();
    }

    public static FriendDao getInstance() {
        return InstanceHolder.DEFAULT;
    }

    private FriendDao(){

    }

    /**
     * DESC: 添加新的好友关系
     * Created by jinphy, on 2018/2/27, at 10:21
     */
    public Result addFriend(String account, String owner) {
        return Database.insert()
                .tables(Database.TABLE_FRIEND)
                .columnNames(Friend.ACCOUNT, Friend.OWNER, Friend.STATUS)
                .columnValues(account, owner, Friend.STATUS_WAITING)
                .execute();
    }

    /**
     * DESC: 删除好友
     * Created by jinphy, on 2018/2/27, at 10:25
     */
    public Result deleteFriend(String account, String owner) {
        return Database.delete()
                .tables(Database.TABLE_FRIEND)
                .whereEq(Friend.ACCOUNT, account)
                .whereEq(Friend.OWNER, owner)
                .execute();
    }

    /**
     * DESC: 将指定的好友拉入黑名单
     * 黑名单的状态是相对于owner而言的：
     *  如果是owner发起的动作，则status=Friend.STATUS_BLACK_LISTING
     *  如果不是owner发起的动作，则status=Friend.STATUS_BLACK_LISTED
     *
     *
     * Created by jinphy, on 2018/2/27, at 10:30
     */
    public Result setStatus(String account, String owner,String status) {
        return Database.update()
                .tables(Database.TABLE_FRIEND)
                .columnNames(Friend.STATUS)
                .columnValues(status)
                .whereEq(Friend.ACCOUNT, account)
                .whereEq(Friend.OWNER, owner)
                .execute();
    }

    /**
     * DESC: 修改好友的备注
     * Created by jinphy, on 2018/2/27, at 10:44
     */
    public Result modifyRemark(String account, String owner, String remark) {
        return Database.update()
                .tables(Database.TABLE_FRIEND)
                .columnNames(Friend.REMARK)
                .columnValues(remark)
                .whereEq(Friend.ACCOUNT, account)
                .whereEq(Friend.OWNER, owner)
                .execute();
    }

    /**
     * DESC: 加载指定账号对应的所有好友
     * Created by jinphy, on 2018/2/28, at 17:49
     */
    public Result loadFriends(String owner) {

        // 根据owner查询所有的friend表
        Result friends = Database.select()
                .columnNames(Friend.ACCOUNT,Friend.OWNER,Friend.DATE,Friend.STATUS,Friend.REMARK)
                .tables(Database.TABLE_FRIEND)
                .whereEq(Friend.OWNER, owner)
                .execute();
        if (friends.data == null) {
            return friends;
        }

        // 遍历所有查询结果，根据每条记录中的account字段查询该account对用的用户的信息
        // 包括用户的昵称、头像、性别、地址
        Map<String,String> user;
        for (Map<String, String> friend : friends.data) {
            user = Database.select()
                    .columnNames(User.NAME,User.AVATAR,User.SEX,User.ADDRESS)
                    .tables(Database.TABLE_USER)
                    .whereEq(User.ACCOUNT, friend.get(Friend.ACCOUNT))
                    .execute().first;
            if (user != null) {
                friend.put(User.NAME, user.get(User.NAME));
                friend.put(User.AVATAR, user.get(User.AVATAR));
                friend.put(User.SEX, user.get(User.SEX));
                friend.put(User.ADDRESS, user.get(User.ADDRESS));
            }
        }
        return friends;
    }
}
