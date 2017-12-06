package com.jinphy.simplechatserver.models;

import com.jinphy.simplechatserver.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jinphy on 2017/12/5.
 */
public class UrlObject {
    private String path;
    private Map<String,String> params;

    /*
     * DESC: 解析url中的路径和参数
     * description：/user/findUser/?account=15889622379
     * path: /user/findUser
     * params: account=15889622379->toMap
     *
     * Created by jinphy, on 2017/12/5, at 20:36
     */
    private UrlObject(String description) {
        if (StringUtils.isEmpty(description)) {
            return;
        }
        if (description.contains("/?")) {
            String[] split = description.split("/\\?");
            this.path = split[0];
            this.params = StringUtils.toMap(split[1]);
        } else {
            if (description.endsWith("/")) {
                this.path = description.substring(0, description.length() - 1);
            } else {
                this.path = description;
            }
            this.params = new HashMap<>(0);
        }

    }

    /**
     * DESC: 通过description生成UrlObject实例
     * Created by jinphy, on 2017/12/5, at 20:54
     */
    public static UrlObject parse(String description){
        return new UrlObject(description);
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

}
