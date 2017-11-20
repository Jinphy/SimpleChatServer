package com.jinphy.simplechatserver.dao;

import com.jinphy.simplechatserver.db.DBConnectionPool;
import com.jinphy.simplechatserver.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {
    private static UserDao instance = new UserDao();

    private UserDao(){}

    public static UserDao getInstance(){
        return instance;
    }
    public synchronized boolean findUser(String account)throws Exception {
        Connection connection=null;
        boolean result = false;

        DBConnectionPool dbPool = DBConnectionPool.getInstance();
        connection = dbPool.getConnection();
        String sql = "select * from user where account=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, account);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            result =true;
        } else {
            result = false;
        }
        return result;
    }

    public synchronized boolean createNewUser(String account,String password,String date)throws Exception{
        Connection connection=null;
        boolean result = false;

        DBConnectionPool dbPool = DBConnectionPool.getInstance();
        connection = dbPool.getConnection();
        String sql = "insert into user(account,password,date) value(?,?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, account);
        preparedStatement.setString(2, password);
        preparedStatement.setString(3, date);
        int resultRows= preparedStatement.executeUpdate();
        if (resultRows>0) {
            result =true;
        } else {
            result = false;
        }

        return result;
    }

    public synchronized boolean login(String account, String password, String deviceId) throws Exception {
        Connection connection = null;
        boolean result = false;

        DBConnectionPool dbPool = DBConnectionPool.getInstance();
        connection = dbPool.getConnection();
        String sql = "select password from user where account like ? ";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, account);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next() &&
                ("null".equals(password) || password.equals(resultSet.getString("password")))) {
            sql = "update user set status = ?,deviceId = ? where account = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "true");
            preparedStatement.setString(2, deviceId);
            preparedStatement.setString(3, account);
            int i = preparedStatement.executeUpdate();
            if (i == 1) {
                result = true;
            } else {
                result = true;
            }

        } else {
            result = false;
        }
        return result;
    }
}
