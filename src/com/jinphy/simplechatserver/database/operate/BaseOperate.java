package com.jinphy.simplechatserver.database.operate;

import com.jinphy.simplechatserver.constants.StringConst;
import com.jinphy.simplechatserver.utils.ObjectHelper;
import com.jinphy.simplechatserver.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;

import static com.jinphy.simplechatserver.constants.StringConst.*;
import static com.jinphy.simplechatserver.utils.StringUtils.isEmpty;
import static com.jinphy.simplechatserver.utils.StringUtils.wrap;
import static java.lang.String.join;

/**
 * DESC:
 * Created by jinphy on 2018/1/3.
 */
abstract class BaseOperate implements Database.Operate {
    protected String tables;                                              // 对所有操作非空
    protected String limit = "";                                          // 可空
    protected String distinct = "";                                       // 可空
    protected String orderBy = "";                                        // 可空
    protected String groupBy = "";                                        // 可空
    protected List<String> havings;                                       // 可空
    protected List<String> wheres;                                        // 可空
    protected String[] columnNames;                                       // 可空，对于select操作 默认 *
    protected List<Object[]> columnValues;                                // 可控性与columnNames一致


    protected String having = "";                                         // 可空，由havings生成
    protected String where = "";                                          // 可空，由wheres生成
    protected String columns;                                             // 除delete操作外非空，由columnNames和columnValues生成

    protected boolean isOkToDo = true;                                    // 是否可以执行，默认为true

    public BaseOperate() {
        this.columnValues = new LinkedList<>();
        this.havings = new LinkedList<>();
        this.wheres = new LinkedList<>();
    }



    @Override
    public Database.Operate tables(String... tables) {
        this.tables = join(COMMA, tables);
        return this;
    }

    @Override
    public Database.Operate columnNames(String... columnNames) {
        if (columnNames.length == 0) {
//            ObjectHelper.throwRuntime("You must specify the columnNames as you invoke this method!");
            return this;
        }
        this.columnNames = columnNames;
        return this;
    }

    @Override
    public Database.Operate columnValues(Object... values) {
        if (columnNames == null) {
            ObjectHelper.throwRuntime("before invoking this method, " +
                    "you must invoke columnNames() first to setup the column names!");
        }
        if (columnNames.length != values.length) {
            ObjectHelper.throwRuntime("colunmNames.length="+columnNames.length+"," +
                    " values.length="+values.length+". you must make them equal to each other!");
        }
        this.columnValues.add(values);
        return this;
    }

    @Override
    public Database.Operate limit(int indexOffset, int count) {
        if (indexOffset < 0) {
            indexOffset = 0;
        }
        if (count <= 0) {
            return this;
        }
        this.limit = LIMIT + BLANK + indexOffset + COMMA + count;
        return this;
    }

    @Override
    public Database.Operate range(int pageNO, int pageSize) {
        int indexOffset = (pageNO - 1) * pageSize;
        return limit(indexOffset, pageSize);
    }

    @Override
    public Database.Operate distinct(boolean distinct) {
        if (distinct) {
            this.distinct = "distinct";
        } else {
            this.distinct = "";
        }
        return this;
    }

    @Override
    public Database.Operate orderBy(String... orderBy) {
        if (orderBy.length > 0) {
            this.orderBy = ORDER_BY + BLANK + join(COMMA, orderBy);
        }
        return this;
    }

    @Override
    public Database.Operate groupBy(String... groupBy) {
        if (groupBy.length > 0) {
            this.groupBy = GROUP_BY+BLANK+ join(COMMA, groupBy);
        }
        return this;
    }

    @Override
    public Database.Operate having(String having) {
        if (!StringUtils.isTrimEmpty(having)) {
            this.havings.add(having);
        }
        return this;
    }

    @Override
    public Database.Operate where(String where) {
        this.wheres.add(where);
        return this;
    }

    @Override
    public Database.Operate whereEq(String column, Object value) {
        if (!isEmpty(value)) {
            this.wheres.add(column + StringConst.EQ + wrap(value,SINGLE_QUOTATION_MARK));
        }
        return this;
    }

    @Override
    public Database.Operate whereLt(String column, Object value) {
        if (!isEmpty(value)) {
            this.wheres.add(column + StringConst.LT + wrap(value,SINGLE_QUOTATION_MARK));
        }
        return this;
    }

    @Override
    public Database.Operate whereLe(String column, Object value) {
        if (!isEmpty(value)) {
            this.wheres.add(column + StringConst.LE + wrap(value,SINGLE_QUOTATION_MARK));
        }
        return this;
    }

    @Override
    public Database.Operate whereBt(String column, Object value) {
        if (!isEmpty()) {
            this.wheres.add(column + StringConst.BT + wrap(value,SINGLE_QUOTATION_MARK));
        }
        return this;
    }

    @Override
    public Database.Operate whereBe(String column, Object value) {
        if (!isEmpty()) {
            this.wheres.add(column + StringConst.BE + wrap(value,SINGLE_QUOTATION_MARK));
        }
        return this;
    }

    @Override
    public Database.Operate whereLB(String column, Object value) {
        if (!isEmpty(value)) {
            this.wheres.add(column + StringConst.LB + wrap(value,SINGLE_QUOTATION_MARK));
        }
        return this;
    }

    @Override
    public Database.Operate whereLike(String column, Object value) {
        if (!isEmpty(value)) {
            this.wheres.add(column+ wrap(LIKE,BLANK)+ wrap(value,SINGLE_QUOTATION_MARK));
        }
        return this;
    }

    @Override
    public Database.Operate whereBetween(String column, Object from, Object to) {
        if (!isEmpty(from, to)) {
            this.wheres.add(column
                    + wrap(BETWEEN, BLANK)
                    + wrap(from, SINGLE_QUOTATION_MARK)
                    + wrap(AND, BLANK)
                    + wrap(to, SINGLE_QUOTATION_MARK));
        }
        return this;
    }

    @Override
    public Database.Operate whereIn(String column, String...values) {
        List<String> strValues = new LinkedList<>();

        for (Object value : values) {
            if (!isEmpty(value)) {
                strValues.add(wrap(value,SINGLE_QUOTATION_MARK));//用单引号包装值
            }
        }
        if (strValues.size()> 0) {
            String value = wrap(
                    join(COMMA, strValues.toArray(new String[strValues.size()])),
                    LEFT_S_BRACKET,
                    RIGHT_S_BRACKET);
            this.wheres.add(column + wrap(IN, BLANK) + value);
        }
        return this;
    }


    @Override
    public Database.Operate whereNotLike(String column, Object value) {
        if (!isEmpty(value)) {
            this.wheres.add(column+BLANK+NOT+wrap(LIKE,BLANK)+ wrap(value,SINGLE_QUOTATION_MARK));
        }
        return this;
    }

    @Override
    public Database.Operate whereNotBetween(String column, Object from, Object to) {
        if (!isEmpty(from, to)) {
            this.wheres.add(column+BLANK+NOT
                    + wrap(BETWEEN, BLANK)
                    + wrap(from, SINGLE_QUOTATION_MARK)
                    + wrap(AND, BLANK)
                    + wrap(to, SINGLE_QUOTATION_MARK));
        }
        return this;
    }

    @Override
    public Database.Operate whereNotIn(String column, Object...values) {
        List<String> strValues = new LinkedList<>();
        for (Object value : values) {
            if (!isEmpty(value)) {
                strValues.add(wrap(value,SINGLE_QUOTATION_MARK));//用单引号包装值
            }
        }
        if (strValues.size()> 0) {
            String value = wrap(
                    join(COMMA, strValues.toArray(new String[strValues.size()])),
                    LEFT_S_BRACKET,
                    RIGHT_S_BRACKET);
            this.wheres.add(column + BLANK + NOT + wrap(IN, BLANK) + value);
        }
        return this;
    }

    @Override
    public Database.Operate run(Database.Function<Database.Operate, Boolean> run) {
        if (isOkToDo && run != null) {
            try {
                Boolean apply = run.apply(this);
                if (apply==null || !apply) {
                    isOkToDo = false;
                } else {
                    isOkToDo = true;
                }
            } catch (Exception e) {
                isOkToDo = false;
                e.printStackTrace();
            }
        }
        return this;
    }

    abstract protected void checkCondition();
}
