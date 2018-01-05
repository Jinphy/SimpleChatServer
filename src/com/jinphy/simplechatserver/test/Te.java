package com.jinphy.simplechatserver.test;

import com.jinphy.simplechatserver.models.db_models.User;

import java.util.List;

/**
 * DESC:
 * Created by jinphy on 2018/1/4.
 */
public class Te {

    private String code;

    private String msg;

    private List<User> data;

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

    public List<User> getData() {
        return data;
    }

    public void setData(List<User> data) {
        this.data = data;
    }
}
