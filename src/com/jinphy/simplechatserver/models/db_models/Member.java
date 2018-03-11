package com.jinphy.simplechatserver.models.db_models;

/**
 * DESC:
 * Created by jinphy on 2018/3/10.
 */
public class Member {

    public static final String ID = "id";
    public static final String GROUP_NO = "groupNo";
    public static final String ACCOUNT = "account";
    public static final String ALLOW_CHAT = "allowChat";
    public static final String STATUS = "status";
    public static final String STATUS_WAITING = "waiting";
    public static final String STATUS_OK = "ok";


    private transient long id;

    private String groupNo;

    private String account;

    private boolean allowChat = true;

    private String status = STATUS_WAITING;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGroupNo() {
        return groupNo;
    }

    public void setGroupNo(String groupNo) {
        this.groupNo = groupNo;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public boolean isAllowChat() {
        return allowChat;
    }

    public void setAllowChat(boolean allowChat) {
        this.allowChat = allowChat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
