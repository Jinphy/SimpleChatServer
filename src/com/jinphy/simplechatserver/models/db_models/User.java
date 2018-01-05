package com.jinphy.simplechatserver.models.db_models;

public class User {

    public static final String ID = "id";
    public static final String ACCOUNT = "account";
    public static final String NAME = "name";
    public static final String PASSWORD = "password";
    public static final String DATE = "date";
    public static final String SEX = "sex";
    public static final String AVATAR_URL = "avatarUrl";
    public static final String STATUS = "status";
    public static final String ACCESS_TOKEN = "accessToken";


    
    private transient int id;
    private String account;
    private String name;
    private String password;
    private String date;
    private String sex;
    private String avatarUrl;
    private String status;//登录状态
    private String accessToken;

    public User(){}

    public User(int id, String account, String password, String date, String avatorUrl) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.date = date;
        this.avatarUrl = avatorUrl;
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

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", account='" + account + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", date='" + date + '\'' +
                ", sex='" + sex + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", status='" + status + '\'' +
                ", accessToken='" + accessToken + '\'' +
                '}';
    }
}
