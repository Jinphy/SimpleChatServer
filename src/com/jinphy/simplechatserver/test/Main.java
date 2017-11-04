package com.jinphy.simplechatserver.test;


import java.io.BufferedInputStream;
import java.util.Properties;

public class Main {




    public static void main(String[] args) {
        String lock = "";
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                try {
                    wait(100000);
                } catch (InterruptedException e) {
                    System.out.println("interrupted");
                    e.printStackTrace();
                }
                System.out.println("finished");
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                notifyAll();
            }
        }).start();

        Properties properties = new Properties();
        BufferedInputStream in = null;
/*
        try {
            in = new BufferedInputStream(new FileInputStream("config/mysql.properties"));
            properties.load(in);
            Connection connection = null;
            Class.forName(properties.getProperty("driver"));
            connection = DriverManager.getConnection(
                    properties.getProperty("url"),
                    properties.getProperty("user"),
                    properties.getProperty("password")
            );
            PreparedStatement preparedStatement = connection.prepareStatement("select * from user");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.println("id = " + resultSet.getBigDecimal("id"));
                System.out.println("account = " + resultSet.getString("account"));
                System.out.println("password = " + resultSet.getString("password"));
                System.out.println("date = " + resultSet.getString("date"));
            }
            DatabaseMetaData metaData = connection.getMetaData();
            boolean b = metaData.supportsBatchUpdates();
            System.out.println(b);
            connection.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
*/

    }
}
