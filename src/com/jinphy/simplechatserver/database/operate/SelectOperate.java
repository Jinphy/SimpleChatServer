package com.jinphy.simplechatserver.database.operate;

import com.jinphy.simplechatserver.database.models.DBConnectionPool;
import com.jinphy.simplechatserver.database.models.Result;
import com.jinphy.simplechatserver.utils.ObjectHelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.jinphy.simplechatserver.constants.StringConst.*;
import static com.jinphy.simplechatserver.utils.StringUtils.wrap;
import static java.lang.String.join;

/**
 * DESC: Db select 操作
 * Created by jinphy on 2018/1/3.
 */
class SelectOperate extends BaseOperate {

    @Override
    protected void checkCondition() {
        ObjectHelper.requireNonNull(tables,"table name is null when doing sql checking!");
        if (havings.size() > 0) {
            this.having = HAVING + BLANK + join(wrap(AND,BLANK), havings);
        }
        if (wheres.size() > 0) {
            this.where = WHERE + BLANK + join(wrap(AND,BLANK), wheres);
        }
        if (this.columnNames == null) {
            this.columns = ASTERISK;
        } else {
            this.columns = join(COMMA, this.columnNames);
        }

    }

    @Override
    public String generateSql() {
        if (!isOkToDo) {
            return null;
        }
        checkCondition();

        StringBuilder sql = new StringBuilder();
        System.out.println(sql);

        sql.append(SELECT).append(BLANK)
                .append(distinct).append(BLANK)
                .append(columns).append(BLANK)
                .append(FROM).append(BLANK)
                .append(tables).append(BLANK)
                .append(where).append(BLANK)
                .append(groupBy).append(BLANK)
                .append(having).append(BLANK)
                .append(orderBy);
        return sql.toString();
    }

    @Override
    public Result execute() {
        if (!isOkToDo) {
            return Result.error();
        }
        Statement statement=null;
        String sql = generateSql();
        try {
            Connection connection = DBConnectionPool.getInstance().getConnection();
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            Result ok = Result.parse(resultSet);
            ok.logger += wrap("sql: " + sql, LINE);
            return ok;
        } catch (Exception e) {
            e.printStackTrace();
            Result error = Result.error();
            error.logger += wrap("sql: " + sql, LINE);
            return error;
        }finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
