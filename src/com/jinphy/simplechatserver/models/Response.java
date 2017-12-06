package com.jinphy.simplechatserver.models;

/**
 * Created by jinphy on 2017/12/5.
 */
public class Response{
    public static String YES = "1";
    public static String NO = "0";

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
}
