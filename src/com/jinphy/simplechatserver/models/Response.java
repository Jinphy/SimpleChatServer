package com.jinphy.simplechatserver.models;

import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.GsonUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.jinphy.simplechatserver.constants.StringConst.UTF_8;

/**
 * Created by jinphy on 2017/12/5.
 */
public class Response{

    //----------返回码 --------------------------------------------------------------------------------------
    public static final String YES = "200";
    public static final String NO = "10000";

    // 以 3 开头的错误为接口请求信息错误
    public static final String NO_FIND_USER = "30001";
    public static final String NO_CREATE_USER = "30002";
    public static final String NO_LOGIN = "30003";
    public static final String NO_GET_CODE = "30004";
    public static final String NO_SUBMIT_CODE = "30005";

    // 以4 开头的为客户端错误
    public static final String NO_API_NOT_FUND = "40001";
    public static final String NO_PARAMS_MISSING = "40002";

    // 以5 开头的为服务器错误
    public static final String NO_SERVER = "50001";

    //-------------------------------------------------------------------------------------------------

    private String code;
    private String msg;
    private String data;

    public Response() {

    }

    public Response(String code, String msg, String data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        String out = GsonUtils.toJson(this);
        try {
            out = URLEncoder.encode(out, UTF_8);
            out = EncryptUtils.aesEncrypt(out);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return out;
    }
}
