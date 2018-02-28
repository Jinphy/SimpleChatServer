package com.jinphy.simplechatserver.database.operate;

import com.jinphy.simplechatserver.database.models.DBConnectionPool;
import com.jinphy.simplechatserver.database.models.Result;
import com.jinphy.simplechatserver.utils.ObjectHelper;

import java.sql.Connection;
import java.sql.Statement;

import static com.jinphy.simplechatserver.constants.StringConst.*;
import static com.jinphy.simplechatserver.constants.StringConst.AND;
import static com.jinphy.simplechatserver.utils.StringUtils.wrap;
import static java.lang.String.join;

/**
 * DESC: DB update 操作
 *
 * Created by jinphy on 2018/1/3.
 */
class UpdateOperate extends BaseOperate {


    @Override
    protected void checkCondition() {
        ObjectHelper.requireNonNull(tables,"table name is null when doing sql checking!");
        if (this.columnNames == null) {
            ObjectHelper.throwRuntime("You must setup the columns to insert values!");
        }
        if (this.columnValues.size() == 0) {
            ObjectHelper.throwRuntime("You must setup the values corresponding to columnNames for inserting!");
        }
        // 一条数据记录中列的列名和值对应的数据，例如：namesAndValues[0]: account='123'
        String[] namesAndValues = new String[this.columnNames.length];
        String columnValue;
        // 列索引
        int i=0;
        Object[] tempValues = this.columnValues.get(0);
        for (Object tempValue : tempValues) {
            // 把每条记录中的所有值的首尾都加上单引号
            columnValue = "";
            if (tempValue!= null) {
                columnValue = tempValue.toString();
            }
            // 将列名和该列对应的加上单引号后的值用“=”拼接，例如 account='123'
            namesAndValues[i] = this.columnNames[i++]+EQ+wrap(columnValue,SINGLE_QUOTATION_MARK);
        }
        this.columns = join(COMMA, namesAndValues);

        if (wheres.size() > 0) {
            this.where = WHERE + BLANK + join(AND, wheres);
        }
    }

    @Override
    public String generateSql() {
        if (!isOkToDo) {
            return null;
        }
        checkCondition();

        StringBuilder sql = new StringBuilder();
        sql.append(UPDATE).append(BLANK)
                .append(tables).append(BLANK)
                .append(SET).append(BLANK)
                .append(columns).append(BLANK)
                .append(where);
        return sql.toString();
    }

    @Override
    public Result execute() {
        if (!isOkToDo) {
            return Result.error();
        }
        String sql = generateSql();
        System.out.println("sql====>>"+sql);
        try {
            Connection connection = DBConnectionPool.getInstance().getConnection();
            Statement statement = connection.createStatement();
            int count = statement.executeUpdate(sql);
            statement.close();
            Result ok = Result.ok(count, null, null);
            ok.logger += wrap("sql: " + sql, LINE);
            return ok;
        } catch (Exception e) {
            e.printStackTrace();
            Result error = Result.error();
            error.logger += wrap("sql: " + sql, LINE);
            return error;
        }
    }
}
