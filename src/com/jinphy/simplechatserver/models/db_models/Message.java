package com.jinphy.simplechatserver.models.db_models;

/**
 * DESC:
 * Created by jinphy on 2018/1/16.
 */
public class Message {

    public static final String ID = "id";
    public static final String FROM = "fromAccount";
    public static final String TO = "toAccount";
    public static final String IS_NEW = "isNew";
    public static final String CREATE_TIME = "createTime";
    public static final String CONTENT = "content";
    public static final String CONTENT_TYPE= "contentType";


    /**
     * DESC: 系统消息
     * Created by jinphy, on 2018/1/16, at 15:24
     */
    public static final String TYPE_SYSTEM_ADD_FRIEND = "system_add_friend";

    public static final String TYPE_SYSTEM_ACCOUNT_INVALIDATE = "system_account_invalidate";

    /**
     * DESC: 添加好友消息
     * Created by jinphy, on 2018/1/16, at 15:24
     */
    public static final String TYPE_FRIEND = "friend";

    /**
     * DESC: 聊天的文本消息
     * Created by jinphy, on 2018/1/16, at 15:24
     */
    public static final String TYPE_CHAT_TEXT = "text";

    /**
     * DESC: 聊天的图片消息
     * Created by jinphy, on 2018/1/16, at 15:25
     */
    public static final String TYPE_CHAT_IMAGE = "image";


    private transient int id;

    /**
     * DESC: 消息的来源账号
     *
     * 默认来自系统 代号0
     * Created by jinphy, on 2018/1/16, at 15:17
     */
    private String fromAccount = "0";

    /**
     * DESC: 消息的接收账号
     * Created by jinphy, on 2018/1/16, at 15:17
     */
    private String toAccount;

    /**
     * DESC: 判断是否为未读消息
     *  两个值：
     *      1、true：  未读消息
     *      2、false： 已读消息
     * Created by jinphy, on 2018/1/16, at 15:18
     */
    private String isNew = "true";

    /**
     * DESC: 消息的创建时间
     * Created by jinphy, on 2018/1/16, at 15:19
     */
    private String createTime;

    /**
     * DESC: 消息的内容
     * Created by jinphy, on 2018/1/16, at 15:19
     */
    private String content;

    /**
     * DESC: 消息的内容类型，例如文本，图片等
     * Created by jinphy, on 2018/1/16, at 15:20
     */
    private String contentType;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public String getIsNew() {
        return isNew;
    }

    public void setIsNew(String isNew) {
        this.isNew = isNew;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
