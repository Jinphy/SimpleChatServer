package com.jinphy.simplechatserver.models.db_models;

import com.jinphy.simplechatserver.constants.StringConst;
import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.GsonUtils;
import com.jinphy.simplechatserver.utils.StringUtils;
import kotlin.text.StringsKt;

import static com.jinphy.simplechatserver.constants.StringConst.FALSE;
import static com.jinphy.simplechatserver.constants.StringConst.TRUE;
import static com.jinphy.simplechatserver.utils.StringUtils.noEqual;

/**
 * DESC:
 * Created by jinphy on 2018/1/4.
 */
public class AccessToken {
    public static final String REASON_DEVICE_CHANGED = "您的账号已在其他设备，请重新登录！";
    public static final String REASON_TIMEOUT = "您的账号验证已过期，请重新登录！";
    public static final String REASON_LOGIN_OUT = "未登录";
    public static final String OK = "OK";

    /**
     * DESC: accessToken 有效期30天
     * Created by jinphy, on 2018/1/4, at 15:34
     */
    public static final long TIMEOUT = 30L*24L*60L*60L*1000L;

    private String deviceId;

    private String loginTime;

    private String status;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }


    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * DESC: 私有化
     * Created by jinphy, on 2018/1/4, at 14:43
     */
    private AccessToken(){}


    /**
     * DESC: 创建一个AccessToken
     * Created by jinphy, on 2018/1/4, at 14:42
     */
    public static AccessToken make(String deviceId,String status) {
        AccessToken out = new AccessToken();
        out.deviceId = deviceId;
        out.status = status;
        out.loginTime = System.currentTimeMillis() + "";
        return out;
    }

    /**
     * DESC: 获取AccessToken失效的原因
     *
     *
     *
     * @param server 服务器中的AccessToken
     * @param client 客户端传过来的AccessToken
     * Created by jinphy, on 2018/1/4, at 15:37
     */
    public static String check(String server,String client) {
        if (noEqual(server, client)) {
            return REASON_DEVICE_CHANGED;
        }
        AccessToken s = GsonUtils.toBean(EncryptUtils.decryptThenDecode(server), AccessToken.class);
        AccessToken c = GsonUtils.toBean(EncryptUtils.decryptThenDecode(client), AccessToken.class);
        if (noEqual(User.STATUS_LOGIN, c.status.toUpperCase())) {
            return REASON_LOGIN_OUT;
        }
        if (c.isTimeout()) {
            return REASON_TIMEOUT;
        }
        return OK;
    }

    // 判断是否登录超时
    private boolean isTimeout() {
        long time = Long.valueOf(loginTime);
        long now = System.currentTimeMillis();
        System.out.println(now - time);
        System.out.println("timeout= "+TIMEOUT);
        return now - time > TIMEOUT;
    }

    @Override
    public String toString() {
        String s = GsonUtils.toJson(this);
        return EncryptUtils.encodeThenEncrypt(s);
    }
}
