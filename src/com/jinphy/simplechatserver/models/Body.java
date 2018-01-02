package com.jinphy.simplechatserver.models;

import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.GsonUtils;
import com.jinphy.simplechatserver.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import static com.jinphy.simplechatserver.constants.StringConst.UTF_8;

/**
 * DESC: Post 请求方法中的body
 *
 * Created by jinphy on 2018/1/2.
 */
public class Body {
    private String requestId;
    private String content;
    private transient Map<String,String> contentMap;

    public Body(String requestId, String content) {
        this.requestId = requestId;
        this.content = content;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * DESC: 解析body中的参数
     *
     *
     * Created by jinphy, on 2018/1/2, at 10:14
     */
    public static Body parse(String bodyStr) {
        System.out.println("bodyStr:" + bodyStr);
        Body body = GsonUtils.toBean(bodyStr, Body.class);
        String content = body.getContent();
        try {
            content = EncryptUtils.aesDecrypt(content);
            content = URLDecoder.decode(content, UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println("requestId: = " + body.getRequestId());
        System.out.println("content: " + content);
        body.contentMap = StringUtils.toMap(content);
        return body;
    }

    public Map<String, String> getContentMap() {
        return contentMap;
    }
}
