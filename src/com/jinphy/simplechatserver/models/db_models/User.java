package com.jinphy.simplechatserver.models.db_models;

public class User {

    public static final String ID = "id";
    public static final String ACCOUNT = "account";
    public static final String NAME = "name";
    public static final String PASSWORD = "password";
    public static final String DATE = "date";
    public static final String SEX = "sex";
    public static final String AVATAR = "avatar";
    public static final String STATUS = "status";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String SIGNATURE = "signature";
    public static final String ADDRESS = "address";

    public static final String STATUS_LOGIN = "LOGIN";
    public static final String STATUS_LOGOUT = "LOGOUT";

    
    private transient int id;
    private String account;
    private String name;
    private String password;
    private String date;
    private String sex;
    private String avatar;
    private String status;//登录状态
    private String accessToken;
    private String signature;// 个性签名
    private String address;

    public User(){}

    public User(int id, String account, String password, String date, String avatorUrl) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.date = date;
        this.avatar = avatorUrl;
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

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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

    public String getAvatar() {
        return avatar;
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
                ", avatar='" + avatar + '\'' +
                ", status='" + status + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", signature='" + signature + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
