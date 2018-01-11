package com.jinphy.simplechatserver.models.network_models;

import java.util.Map;

/**
 * DESC:
 * Created by jinphy on 2018/1/10.
 */
public class KeyValueArray {

    public String[] keys;

    public String[] values;

    public KeyValueArray(int size) {
        keys = new String[size];
        values = new String[size];
    }

    public static KeyValueArray parse(Map<String, String> params) {
        if (params != null && params.size() > 0) {
            KeyValueArray array = new KeyValueArray(params.size());
            int i = 0;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                array.keys[i] = entry.getKey();
                array.values[i++] = entry.getValue();
            }
            return array;
        }
        return null;
    }
}
