package com.jinphy.simplechatserver.database.operate;

import com.jinphy.simplechatserver.database.models.DBConnectionPool;
import com.jinphy.simplechatserver.database.models.Result;
import com.jinphy.simplechatserver.utils.ObjectHelper;

import java.sql.Connection;
import java.sql.Statement;

import static com.jinphy.simplechatserver.constants.StringConst.*;
import static com.jinphy.simplechatserver.utils.StringUtils.wrap;
import static java.lang.String.join;

/**
 * DESC: DB insert 操作
 * Created by jinphy on 2018/1/3.
 */
class InsertOperate extends BaseOperate {
    @Override
    protected void checkCondition() {
        ObjectHelper.requireNonNull(tables,"table name is null when doing sql checking!");
        if (this.columnNames == null) {
            ObjectHelper.throwRuntime("You must setup the columns to insert values!");
        }
        if (this.columnValues.size() == 0) {
            ObjectHelper.throwRuntime("You must setup the values corresponding to columnNames for inserting!");
        }
        // 总数据记录
        String[] items = new String[this.columnValues.size()];
        // 单条数据记录
        String[] item = new String[this.columnNames.length];
        // 某列对应的值
        String columnValue;
        // 当前记录的索引
        int i = 0;
        for (Object[] tempItem : this.columnValues) {
            // 一个tempItem 代表一条数据记录

            // 列索引
            int j=0;
            for (Object tempValue : tempItem) {
                // 把每条记录中的所有值的首尾都加上单引号
                columnValue = "";
                if (tempValue!= null) {
                    columnValue = tempValue.toString();
                }
                // 每个值首尾都加上单引号，例如：'hello'
                item[j++] = wrap(columnValue, SINGLE_QUOTATION_MARK);
            }
            // 每条记录的数据用逗号隔开，有括号括起来，例如：(...,...,...,...,...)
            items[i++] = wrap(join(COMMA, item), LEFT_S_BRACKET, RIGHT_S_BRACKET);
        }

        // 列名用逗号隔开，然后用括号括起来，例如：(account,password,age)
        String names = wrap(join(COMMA,columnNames),LEFT_S_BRACKET,RIGHT_S_BRACKET);

        // 多条记录用逗号隔开，例如 (...),(...),(...)
        String values = join(COMMA, items);

        // 例如：(account,password,age) VALUE ('123','123',''18)
        this.columns = names + wrap(VALUES, BLANK) + values;
    }

    @Override
    public String generateSql() {
        if (!isOkToDo) {
            return null;
        }
        checkCondition();

        StringBuilder sql = new StringBuilder();
        sql.append(INSERT).append(BLANK)
                .append(INTO).append(BLANK)
                .append(tables).append(BLANK)
                .append(columns);

        return sql.toString();
    }

    @Override
    public Result execute() {
        if (!isOkToDo) {
            return Result.error();
        }
        String sql = generateSql();
        Connection connection = null;
        try {
            connection = DBConnectionPool.getInstance().getConnection();
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
        }finally {
            if (connection != null) {
                DBConnectionPool.getInstance().recycle(connection);
            }
        }
    }
}
