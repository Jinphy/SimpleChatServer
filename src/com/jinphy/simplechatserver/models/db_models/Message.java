package com.jinphy.simplechatserver.models.db_models;

import com.google.gson.reflect.TypeToken;
import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.GsonUtils;
import com.jinphy.simplechatserver.utils.StringUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

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
    public static final String EXTRA = "extra";


    /**
     * DESC: 系统消息
     * Created by jinphy, on 2018/1/16, at 15:24
     */
    public static final String TYPE_SYSTEM_ADD_FRIEND = "system_add_friend";

    public static final String TYPE_SYSTEM_ACCOUNT_INVALIDATE = "system_account_invalidate";

    public static final String TYPE_SYSTEM_NOTICE = "system_notice";

    public static final String TYPE_SYSTEM_ADD_FRIEND_AGREE = "system_add_friend_agree";

    public static final String TYPE_SYSTEM_RELOAD_FRIEND = "system_reload_friend";

    public static final String TYPE_SYSTEM_DELETE_FRIEND = "system_delete_friend";

    public static final String TYPE_SYSTEM_NEW_GROUP = "system_new_group";

    public static final String TYPE_SYSTEM_RELOAD_GROUP = "system_reload_group";

    public static final String TYPE_SYSTEM_NEW_MEMBER = "system_new_member";

    public static final String TYPE_SYSTEM_APPLY_JOIN_GROUP = "system_apply_join_group";

    public static final String TYPE_SYSTEM_DELETE_MEMBER = "system_delete_member";

    public static final String TYPE_SYSTEM_BREAK_GROUP = "system_break_group";

    public static final String TYPE_SYSTEM_MEMBER_ALLOW_CHAT = "system_member_allow_chat";

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

    public static final String KEY_SENDER = "KEY_SENDER";

    public static final String KEY_GROUP_NO = "KEY_GROUP_NO";

    public static final String KEY_CHAT_GROUP = "KEY_CHAT_GROUP";


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

    /**
     * DESC: 额外的信息
     * Created by jinphy, on 2018/3/2, at 9:13
     */
    private String extra;

    transient private Map<String, String> extraMap = new HashMap<>();

    private static Type mapType = new TypeToken<Map<String, String>>() {
    }.getType();


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

    public String getExtra() {
        if (StringUtils.isEmpty(extra) && extraMap != null && extraMap.size() > 0) {
            extra = GsonUtils.toJson(extraMap);
        }
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
        if (extra != null) {
            try {
                Map<String, String> tempMap = GsonUtils.toBean(extra, mapType);
                if (tempMap != null && tempMap.size() > 0) {
                    extraMap.clear();
                    for (Map.Entry<String, String> entry : tempMap.entrySet()) {
                        extraMap.put(entry.getKey(), entry.getValue());
                    }
                }
            } catch (Exception e) {
            }
        }
    }


    /**
     * DESC: 保存extra信息
     * Created by jinphy, on 2018/3/18, at 19:20
     */
    public void extra(String key, Object value) {
        if (value != null) {
            extraMap.put(key, EncryptUtils.aesEncrypt(value.toString()));
        }
    }


    /**
     * DESC: 通过key获取extra信息
     * Created by jinphy, on 2018/3/18, at 19:20
     */
    public String extra(String key) {
        if (key == null) {
            return "";
        }
        String value = extraMap.get(key);

        return EncryptUtils.aesDecrypt(value);
    }

    public String removeExtra(String key) {
        if (extraMap != null) {
            String remove = extraMap.remove(key);
            return EncryptUtils.aesDecrypt(remove);
        }
        return null;
    }
}
