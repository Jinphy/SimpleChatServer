package com.jinphy.simplechatserver.models;

import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.GsonUtils;
import com.jinphy.simplechatserver.utils.StringUtils;
import org.java_websocket.handshake.ClientHandshake;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import static com.jinphy.simplechatserver.constants.StringConst.UTF_8;

/**
 * Created by jinphy on 2017/12/5.
 */
public class Request {

    private String method;                // 请求方法
    private String requestId;             // 每次请求的唯一id,就算是同一台设备请求同一个接口也不会相同
    private String path;                  // 请求接口
    private Map<String,String> params;    // 请求参数，包括url中的和body中的（如果有的话）

    /*
     * DESC: 解析url中的路径和参数
     * path: /user/findUser
     * params: account=15889622379->toMap
     *
     * Created by jinphy, on 2017/12/5, at 20:36
     */
    private Request(String method, String requestId, String path, Map<String, String> params) {
        this.method = method;
        this.requestId = requestId;
        this.path = path;
        this.params = params;
    }

    /**
     * DESC: 解析Get请求方法的参数，通过description生成UrlObject实例
     *
     * description：/user/findUser/?content=JIEFFIHGANVIAEFAIJGEFAIAOHAIGI
     *  content的值是一个编码、加密后的字符串
     *  path = /user/findUser
     *  content = JIEFFIHGANVIAEFAIJGEFAIAOHAIGI
     * Created by jinphy, on 2017/12/5, at 20:54
     */
    public static Request parse(ClientHandshake handshake){
        System.out.println("description: " + handshake.getResourceDescriptor());

        String[] split = handshake.getResourceDescriptor().split("/\\?content=");
        String path = split[0];
        String content = "";
        if (split.length > 1) {
            content = split[1];
        }
        try {
            content = EncryptUtils.aesDecrypt(content);
            content = URLDecoder.decode(content, UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String method = handshake.getFieldValue("method");
        String requestId = handshake.getFieldValue("requestId");
        System.out.println("method: "+ method);
        System.out.println("requestId: " + requestId);
        System.out.println("path: " + path);
        System.out.println("content: " + content);
        return new Request(method.toUpperCase(), requestId, path, StringUtils.toMap(content));
    }



    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * DESC: 获取请求路径
     * Created by jinphy, on 2017/12/5, at 20:52
     */
    public String getPath() {
        return path;
    }

    /**
     * DESC: 获取请求参数
     * Created by jinphy, on 2017/12/5, at 20:52
     */
    public Map<String, String> getParams() {
        return params;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void addParams(Map<String, String> params) {
        this.params.putAll(params);
    }
}
