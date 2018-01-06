package com.jinphy.simplechatserver.database.dao;

import com.jinphy.simplechatserver.constants.StringConst;
import com.jinphy.simplechatserver.database.models.DBConnectionPool;
import com.jinphy.simplechatserver.database.models.Result;
import com.jinphy.simplechatserver.database.operate.Database;
import com.jinphy.simplechatserver.models.db_models.AccessToken;
import com.jinphy.simplechatserver.models.db_models.User;
import com.jinphy.simplechatserver.utils.GsonUtils;

import java.sql.*;

public class UserDao {
    private static UserDao instance = new UserDao();

    private UserDao(){}

    public static UserDao getInstance(){
        return instance;
    }
    public synchronized Result findUser(String account)  {
        return Database.select()
                .tables(Database.TABLE_USER)
                .whereEq(User.ACCOUNT, account)
                .execute();
    }

    public synchronized Result createNewUser(String account,String password,String date) {
        return Database.insert()
                .tables(Database.TABLE_USER)
                .columnNames(User.ACCOUNT, User.PASSWORD, User.DATE)
                .columnValues(account, password, date)
                .execute();
    }

    public synchronized Result login(String account, String password, String deviceId) {
        // 查询当前账号account用户的密码
        Result result = findUser(account);

        if (result.count > 0) {
            // 账号存在，正常情况下count=1
            String accessToken = AccessToken.make(deviceId).toString();
            result.first.put(User.ACCESS_TOKEN, accessToken);
            result.first.put(User.STATUS, StringConst.TRUE.toLowerCase());

            if ("null".equals(password) || password.equals(result.first.get(User.PASSWORD))) {
                // 密码正确
                Result update = Database.update()
                        .tables(Database.TABLE_USER)
                        .columnNames(User.STATUS, User.ACCESS_TOKEN)
                        .columnValues(true, accessToken)
                        .whereEq(User.ACCOUNT, account)
                        .execute();
                if (update.count > 0) {
                    // 更新数据库成功，登录成功
                    result.first.remove(User.ID);
                    result.first.remove(User.PASSWORD);
                    return result;
                } else {
                    // 跟新数据库失败，服务器异常，登录失败
                    return Result.error();
                }
            } else {
                // 密码错误
                return Result.ok(result.count, null, null);
            }
        }
        // 账号不存在
        return result;
    }
}
