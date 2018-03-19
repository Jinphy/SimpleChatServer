package com.jinphy.simplechatserver.models.network_models;

import com.jinphy.simplechatserver.constants.StringConst;
import com.jinphy.simplechatserver.utils.EncryptUtils;
import com.jinphy.simplechatserver.utils.GsonUtils;
import com.jinphy.simplechatserver.utils.StringUtils;

import java.util.Map;

import static com.jinphy.simplechatserver.constants.StringConst.LINE;

/**
 * DESC: Post 请求方法中的body
 *
 * Created by jinphy on 2018/1/2.
 */
public class Body {
    private String requestId;
    private String content;
    private transient Map<String,String> contentMap;

    public String loggor;

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
        Body body = GsonUtils.toBean(bodyStr, Body.class);
        // 解密和反编码
        String content = EncryptUtils.decryptThenDecode(body.getContent());
        body.contentMap = StringUtils.toMap(content);

        body.loggor = "bodyStr:" + bodyStr+ LINE
                +"body requestId: = " + body.getRequestId()+LINE
                +"body content: " + content+LINE;
        return body;
    }

    public Map<String, String> getContentMap() {
        return contentMap;
    }
}
