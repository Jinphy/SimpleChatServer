package com.jinphy.simplechatserver.database.models;

import com.jinphy.simplechatserver.utils.GsonUtils;
import com.jinphy.simplechatserver.utils.ObjectHelper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * DESC: 数据库操作结果类
 *
 * Created by jinphy on 2018/1/3.
 */
public class Result {

    /**
     * DESC: 操作数据库影响的记录数
     *  1、当count = -1 时，表示操作异常
     *  2、当count = 0 时，表示操作正确，但是操作结果影响的条数为0
     *  3、当count > 0 时，表示操作正确，且影响的条数为count
     * Created by jinphy, on 2018/1/3, at 20:20
     */
    public final int count;

    /**
     * DESC: 查询数据库的结果，以json格式返回
     * 1、当是select操作时，如果操作成果返回结果
     * 2、当是insert、update、delete时，为null
     * Created by jinphy, on 2018/1/3, at 20:20
     */
    public final List<Map<String, String>> data;

    /**
     * DESC: 查询数据库的结果的第一条记录，以json格式返回
     * Created by jinphy, on 2018/1/4, at 15:50
     */
    public final Map<String, String> first;


    public transient String logger;

    public Result(int count, List<Map<String, String>> jsonData, Map<String, String> first) {
        this.count = count;
        this.data = jsonData;
        this.first = first;
        this.logger = GsonUtils.toJson(this);
    }

    public static Result parse(ResultSet resultSet) {
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            String[] columnNames = new String[columnCount];
            // 列的索引从1开始
            for (int i = 1; i <= columnCount; i++) {
                columnNames[i - 1] = metaData.getColumnName(i);
            }

            List<Map<String, String>> jsonList = new LinkedList<>();
            Map<String, String> entity;

            // resultSet的游标刚开始在第一条记录的前面，所以要先调用next方法把游标指定到第一条记录
            while (resultSet.next()) {
                entity = new HashMap<>();
                // 处理当前resultSet游标所在的记录
                for (String columnName : columnNames) {
                    Object value = resultSet.getObject(columnName);
                    if (value != null) {
                        entity.put(columnName, value.toString());
                    }
                }
                jsonList.add(entity);
            }

            int count = jsonList.size();
            return Result.ok(count, jsonList, count > 0 ? jsonList.get(0) : null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error();
        }
    }

    public static Result error() {
        return new Result(-1, null, null);
    }

    public static Result ok(int count, List<Map<String, String>> jsonData, Map<String, String> first) {
        if (count < 0) {
            ObjectHelper.throwRuntime("The count must be big than 0 when you invoke this method!");
        }
        return new Result(count, jsonData, first);
    }

}
