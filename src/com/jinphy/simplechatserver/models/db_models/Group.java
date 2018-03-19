package com.jinphy.simplechatserver.models.db_models;

/**
 * DESC:
 * Created by jinphy on 2018/3/10.
 */
public class Group {

    public static final String ID = "id";
    public static final String CREATOR = "creator";
    public static final String OWNER = "owner";
    public static final String NAME = "name";
    public static final String GROUP_NO = "groupNo";
    public static final String MAX_COUNT = "maxCount";
    public static final String AUTO_ADD = "autoAdd";
    public static final String SHOW_MEMBER_NAME = "showMemberName";
    public static final String KEEP_SILENT = "keepSilent";
    public static final String REJECT_MSG = "rejectMsg";
    public static final String AVATAR = "avatar";

    public static final String STATUS_WAITING = "waiting";


    private transient long id;

    private String creator;

    private String owner;

    private String name;

    private String groupNo;

    private String maxCount;

    private String autoAdd;

    private String showMemberName;

    private String keepSilent;

    private String rejectMsg;

    private String avatar;



    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupNo() {
        return groupNo;
    }

    public void setGroupNo(String groupNo) {
        this.groupNo = groupNo;
    }

    public String getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(String maxCount) {
        this.maxCount = maxCount;
    }

    public String getAutoAdd() {
        return autoAdd;
    }

    public void setAutoAdd(String autoAdd) {
        this.autoAdd = autoAdd;
    }

    public String getShowMemberName() {
        return showMemberName;
    }

    public void setShowMemberName(String showMemberName) {
        this.showMemberName = showMemberName;
    }

    public String getKeepSilent() {
        return keepSilent;
    }

    public void setKeepSilent(String keepSilent) {
        this.keepSilent = keepSilent;
    }

    public String getRejectMsg() {
        return rejectMsg;
    }

    public void setRejectMsg(String rejectMsg) {
        this.rejectMsg = rejectMsg;
    }


    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
