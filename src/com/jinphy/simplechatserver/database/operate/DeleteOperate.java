package com.jinphy.simplechatserver.database.operate;

import com.jinphy.simplechatserver.database.models.DBConnectionPool;
import com.jinphy.simplechatserver.database.models.Result;
import com.jinphy.simplechatserver.utils.ObjectHelper;

import java.sql.Connection;
import java.sql.Statement;

import static com.jinphy.simplechatserver.constants.StringConst.*;
import static com.jinphy.simplechatserver.utils.StringUtils.wrap;

/**
 * DESC: Db delete 操作
 * Created by jinphy on 2018/1/3.
 */
class DeleteOperate extends BaseOperate {

    @Override
    protected void checkCondition() {
        ObjectHelper.requireNonNull(tables,"table name is null when doing sql checking!");
        if (wheres.size() > 0) {
            this.where = WHERE + BLANK + String.join(wrap(AND), wheres);
        }
    }

    @Override
    public String generateSql() {
        if (!isOkToDo) {
            return null;
        }
        checkCondition();

        StringBuilder sql = new StringBuilder();
        sql.append(DELETE).append(BLANK)
                .append(FROM).append(BLANK)
                .append(tables).append(BLANK)
                .append(where);

        return sql.toString();
    }

    @Override
    public Result execute() {
        if (!isOkToDo) {
            return Result.error();
        }
        String sql = generateSql();
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
