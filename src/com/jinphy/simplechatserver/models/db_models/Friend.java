package com.jinphy.simplechatserver.models.db_models;

import com.jinphy.simplechatserver.database.dao.FriendDao;

/**
 * DESC:
 * Created by jinphy on 2018/2/27.
 */
public class Friend {

    public static final String ID = "id";

    public static final String ACCOUNT = "account";

    public static final String OWNER = "owner";

    public static final String DATE = "date";

    public static final String REMARK = "remark";

    public static final String STATUS = "status";


    public static final String STATUS_OK = "ok";

    public static final String STATUS_WAITING = "waiting";

    public static final String STATUS_BLACK_LISTED = "blackListed";

    public static final String STATUS_BLACK_LISTING = "blackListing";


    private transient int id;

    /**
     * DESC: 该好友对应账号
     * Created by jinphy, on 2018/2/27, at 9:46
     */
    private String account;

    /**
     * DESC: 该好友的拥有者（是一个账号）
     * Created by jinphy, on 2018/2/27, at 9:46
     */
    private String owner;

    /**
     * DESC: 该好友成立的日期，即两个账号双方确认为好友的日期
     * Created by jinphy, on 2018/2/27, at 9:49
     */
    private String date;

    /**
     * DESC: 标志是否已经认证，即添加好友后被添加方是否已经同意
     *
     *      取值
     *          ok：正常好友
     *          waiting：表示还未认证，正在等待状态
     *          blackListed：被拉入黑名单
     *          blackListing：拉入黑名单
     *          注意，如果被添加方已经拒绝好友申请，则后台数据库会自动将该好友关系删除，所以此时不存在该好友
     * Created by jinphy, on 2018/2/27, at 9:51
     */
    private String status;

    private String remark;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * DESC: 为指定的账号添加默认的好友
     * Created by jinphy, on 2018/2/27, at 16:43
     */
    public static void addDefault(String account) {
        // 默认添加系统为好友，系统的代号为0
        FriendDao.getInstance().addFriend("0", account);
        FriendDao.getInstance().setStatus("0", account, Friend.STATUS_WAITING);
    }
}
