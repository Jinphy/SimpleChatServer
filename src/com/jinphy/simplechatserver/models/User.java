package com.jinphy.simplechatserver.models;

public class User {
    private int id;
    private String account;
    private String name;
    private String password;
    private String date;
    private String avatorUrl;

    public User(){}

    public User(int id, String account, String password, String date, String avatorUrl) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.date = date;
        this.avatorUrl = avatorUrl;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setAvatorUrl(String avatorUrl) {
        this.avatorUrl = avatorUrl;
    }

    public int getId() {
        return id;
    }

    public String getAccount() {
        return account;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getDate() {
        return date;
    }

    public String getAvatorUrl() {
        return avatorUrl;
    }
}
