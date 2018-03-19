package com.jinphy.simplechatserver.database.models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;



public class DBConnectionPool {
//    连接池单例
    private static DBConnectionPool instance = new DBConnectionPool();
//    连接池当前创建的连接数
    private static int currentConnectionNums = 0;
//    连接池中的空闲连接
    private List<MyConnection> connections = new LinkedList<>();
//    数据库的配置信息
    private final DBConfig dbConfig = new DBConfig();
//    标志是否有启动后台线程
    private boolean hasRunBackgroungThread = false;
/*
     一个后台线程，用来检测是否数据库连接池中的连接数超过了正常状态下的连接数，
    如果超过了，则把超过部分的长时间未被使用的连接关闭，否则（连接数没有超过）
    就终止该后台线程
    */
    private void run(){
        hasRunBackgroungThread = true;
        while (true) {
            try {
                Thread.sleep(dbConfig.connectIdleTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (connections) {
                if (DBConnectionPool.currentConnectionNums > dbConfig.minConnectionNum) {
                    try {
                        while (DBConnectionPool.currentConnectionNums > dbConfig.minConnectionNum
                                && connections.size() > 0) {
                            MyConnection myConnection = connections.get(0);
                            if (System.currentTimeMillis() - myConnection.time > dbConfig.connectIdleTime) {
                                DBConnectionPool.currentConnectionNums--;
                                myConnection.connection.close();
                                connections.remove(0);
                            } else {
                                break;
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
            }
        }
        hasRunBackgroungThread = false;
    }

    private DBConnectionPool() {
        try {
            Class.forName(dbConfig.driver);
            for (int i = 0; i < dbConfig.minConnectionNum; i++) {
                MyConnection myConnection = new MyConnection();
                myConnection.connection = DriverManager.getConnection(dbConfig.url, dbConfig.user, dbConfig.password);
                myConnection.time = System.currentTimeMillis();
                connections.add(myConnection);
            }
            currentConnectionNums = dbConfig.minConnectionNum;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取连接池单例
     * */
    public static DBConnectionPool getInstance() {
        return instance;
    }

    /**
     * 从连接池中获取一个连接
     *
     * */
    public Connection getConnection() throws SQLException, InterruptedException {
        synchronized (connections) {
            // 连接池中有空闲的连接
            if (connections.size() > 0) {
                return connections.remove(0).connection;
            }
            // 连接池中的连接数还未达到最大连接数
            if (DBConnectionPool.currentConnectionNums < dbConfig.maxConnectionNum) {
                DBConnectionPool.currentConnectionNums++;
                if (DBConnectionPool.currentConnectionNums > dbConfig.minConnectionNum
                        && !hasRunBackgroungThread) {
                    // 连接池中的连接数超过了正常的连接数，并且还没有启动后台线程来关闭超过的连接时，
                    // 启动后台线程来关闭超过部分的并且长时间空闲的连接
                    new Thread(this::run).start();
                }
                return DriverManager.getConnection(dbConfig.url,dbConfig.user,dbConfig.password);
            }
            // 连接池已经达到最大连接数，则等待空闲的连接，并释放连接池
            while (connections.size() <= 0) {
                connections.wait(dbConfig.connectTimeout);
            }

            // 唤醒后说明，池中有空闲的连接了，则使用该连接
            return connections.remove(0).connection;
        }
    }
    /**
     * 该函数用于回收已经用完的连接到连接池中
     * @param connection 已用完将要回收的连接
     *
    * */
    public void recycle(Connection connection){
        synchronized (connections) {
            MyConnection myConnection = new MyConnection();
            myConnection.connection = connection;
            myConnection.time = System.currentTimeMillis();
            connections.add(myConnection);
            connections.notify();
        }
    }

    /*
    * 连接池包装类
    * */
    private class MyConnection{
        Connection connection;
        long time;
    }


}
