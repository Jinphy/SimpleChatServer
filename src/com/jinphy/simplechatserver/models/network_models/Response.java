package com.jinphy.simplechatserver.models.network_models;

import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.GsonUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.jinphy.simplechatserver.constants.StringConst.UTF_8;

/**
 * 泛型类，网络请求返回类，T为要返回的类型，有两种，：
 *  1、一种是List<Map<String,String> >：表示返回某种实体类的列表
 *  2、另一种是Map<String,String>：表示某种实体类
 *
 *      注意Map中的key 是某个实体类的属性字段
 * Created by jinphy on 2017/12/5.
 */
public class Response<T>{

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
    public static final String NO_PARAMS_ERROR = "40003";

    // 以5 开头的为服务器错误
    public static final String NO_SERVER = "50001";

    //-------------------------------------------------------------------------------------------------

    private String code;
    private String msg;
    private T data;

    public Response() {

    }

    public Response(String code, String msg, T data) {
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
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

    /**
     * DESC: 创建一个网络请求返回对象
     *
     *
     * @param data 返回的数据，可以是除了json格式数据外的任意对象，
     *             例如可以是
     *             1、Map<String,String>
     *             2、List<Map<String,String>>
     *             3、User类的对象，等等
     *
     *             注意： 不能是json数据的字符串的原因是因为，如果data字段是json 那么
     *             在response转换成json的时候，data字段又进行json转换，这将导致所有data
     *             字段中的键、值都会加上双引号，从而导致在返回给客户端的时候解析出错
     * Created by jinphy, on 2018/1/4, at 14:01
     */
    public static<U> Response<U> make(String code, String msg, U data) {
        return new Response<>(code, msg, data);
    }
}
