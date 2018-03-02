package com.jinphy.simplechatserver.database.operate;

import com.jinphy.simplechatserver.database.models.DBConnectionPool;
import com.jinphy.simplechatserver.database.models.Result;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DESC:
 * Created by jinphy on 2018/1/3.
 */
public class Database {

    public static final String TABLE_USER = "user";
    public static final String TABLE_FRIEND = "friend";
    public static final String TABLE_MESSAGE = "message";


    /**
     * DESC: 启动事务执行多条sql操作,并且可根据接口入参Statement对象执行批处理操作
     *
     *      注意：
     *      1、批处理操作只能执行insert、update、delete等更新操作，不能执行select查询操作
     *
     *      2、执行批处理操作时
     *          1、执行statement对象的addBatch()方法添加sql语句，sql语句可以通过Operate.generateSql生成
     *              {@link Operate#generateSql()}
     *          2、执行statement对象的executeBatch() 方法执行批处理
     *
     *      3、调用该函数时，你不需要关心当操作失败时否需要回滚，也不需要关心资源是否要关闭
     *          你所要做的就是：
     *          1、执行你要执行的sql语句
     *          2、如果操作失败或者操作结果不符合你要求需要回滚时，只需返回false
     *          2、当所有操作成功时，你必须返回true来提交所有操作结果
     *
     * <p>{@code
     *                  Database.execute(statement -> {
     *                            Database.select().tables(Database.TABLE_USER).execute();
     *                            Database.select().tables(Database.TABLE_USER).execute();
     *                            Database.select().tables(Database.TABLE_USER).execute();
     *                            statement.addBatch(sql1);
     *                            statement.addBatch(sql2);
     *                            statement.addBatch(sql3);
     *                            statement.executeBatch();
     *                            return true;
     *                  });
     *
     *
     *
     *
     *
     *
     *
     *
     *
     * }</p>
     *
     *
     * @param transaction 执行事务的接口回调
     *
     * Created by jinphy, on 2018/1/4, at 8:55
     */
    public static void execute(Function<Statement, Boolean> transaction) {
        if (transaction != null) {
            Connection connection = null;
            Statement statement=null;
            try {
                connection = DBConnectionPool.getInstance().getConnection();
                connection.setAutoCommit(false);
                statement = connection.createStatement();

                // 执行回调
                Boolean apply = transaction.apply(statement);
                if (apply==null || !apply) {
                    // 不成功则回滚
                    connection.rollback();
                } else {
                    // 成功则提交
                    connection.commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }finally {
                DBConnectionPool.getInstance().recycle(connection);
                try {
                    if (connection != null) {
                        connection.setAutoCommit(true);
                    }
                    if (statement != null) {
                        statement.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    /**
     * DESC: 创建插入操作
     * Created by jinphy, on 2018/1/3, at 15:31
     */
    public static Operate insert() {
        return new InsertOperate();
    }

    /**
     * DESC: 创建更新操作
     * Created by jinphy, on 2018/1/3, at 15:31
     */
    public static Operate update() {
        return new UpdateOperate();
    }

    /**
     * DESC: 创建删除操作
     * Created by jinphy, on 2018/1/3, at 21:39
     */
    public static Operate delete() {
        return new DeleteOperate();
    }

    /**
     * DESC: 创建查询操作
     * Created by jinphy, on 2018/1/3, at 21:40
     */
    public static Operate select() {
        return new SelectOperate();
    }

    /**
     * DESC: 数据库操作接口
     * Created by jinphy, on 2018/1/3, at 20:24
     */
    public interface Operate{


        /**
         * DESC: 表名
         * Created by jinphy, on 2018/1/3, at 15:50
         */
        Operate tables(String...tables);


        /**
         * DESC: 需要操作的列名，该方法对insert、update、select有效，delete将会忽略该方法
         *
         * Created by jinphy, on 2018/1/4, at 9:51
         */
        Operate columnNames(String... columnNames);


        /**
         * DESC: 需要操作的列对应的值，值得个数要与设置的列名的个数一致，如果列名为设置或者
         *      个数不一致将会抛出运行时异常，该方法对insert、update有效，delete、select将
         *      会忽略该方法
         *
         *      注意，对于insert操作，该方法可以调用多次，对于update，设置多次只取第一次设置的值
         *
         * Created by jinphy, on 2018/1/4, at 9:53
         */
        Operate columnValues( Object...values);



        /**
         * DESC: 限制查询范围
         *
         * @param indexOffset 查询的开始索引偏移，从0开始计算，即0代表从第一条记录开始插叙
         * @param count 查询的条数
         * Created by jinphy, on 2018/1/3, at 20:55
         */
        Operate limit(int indexOffset, int count);


        /**
         * DESC: 限制查询范围，该方法是对{@link Operate#limit(int, int)} 的封装
         *
         * @param pageNO 查询的页号，从1开始计算
         * @param pageSize 每页的大小
         * Created by jinphy, on 2018/1/3, at 20:59
         */
        Operate range(int pageNO, int pageSize);

        /**
         * DESC: 设置是否去除相同的值
         * Created by jinphy, on 2018/1/3, at 16:38
         */
        Operate distinct(boolean distinct);

        /**
         * DESC: 查询结果的排序方式，可以多次调用，例如
         *      1、orderBy = account asc 按account 升序
         *      2、orderBy = account desc 按account 降序
         *      3、orderBy = account desc, age asc 先按account 降序，在按age 升序
         * Created by jinphy, on 2018/1/3, at 16:51
         */
        Operate orderBy(String... orderBy);

        /**
         * DESC: 分组查询，例如
         *      1、groupBy = account
         *      2、groupBy = account，age
         *
         * Created by jinphy, on 2018/1/3, at 17:01
         */
        Operate groupBy(String... groupBy);

        /**
         * DESC: having条件，当用groupBy后要用这个而不用where，
         *      但是groupBy前是可以用where的.
         *      该方法可以多次调用
         *      例如：
         *      1、havings = count(column_name) > value
         *
         *      SELECT column_name, sql_function(column_name)
         *        FROM table_name
         *        WHERE column_name operator value
         *        GROUP BY column_name
         *        HAVING sql_function(column_name_1) operator value1 and sql_function(column_name_2) operator value2
         *
         * Created by jinphy, on 2018/1/3, at 17:04
         */
        Operate having(String having);

        /**
         * DESC: where条件，该方法可以调用多次
         *
         * @param where 查询条件，例如
         *              1、account = 123
         *              2、name like Bob and age < 233
         * Created by jinphy, on 2018/1/3, at 15:55
         */
        Operate where(String where);

        /**
         * DESC: where等于条件，通过该方法查询的条件为 = ，例如 account=123
         *      该方法可以调用多次
         *
         * @param column 指定条件的列名
         * @param value 列名对应的值
         * Created by jinphy, on 2018/1/3, at 16:12
         */
        Operate whereEq(String column, Object value);


        /**
         * DESC: where小于条件，通过该方法查询的条件为 < ，例如 account<123
         *      该方法可以调用多次
         *
         * @param column 指定条件的列名
         * @param value 列名对应的值
         * Created by jinphy, on 2018/1/3, at 16:12
         */
        Operate whereLt(String column, Object value);


        /**
         * DESC: wheres 小于等于条件，通过该方法查询的条件为 >= ，例如 account <= 123
         *      该方法可以调用多次
         *
         * @param column 指定条件的列名
         * @param value 列名对应的值
         * Created by jinphy, on 2018/1/3, at 16:12
         */
        Operate whereLe(String column, Object value);


        /**
         * DESC: where大于条件，通过该方法查询的条件为 > ，例如 account>123
         *      该方法可以调用多次
         *
         * @param column 指定条件的列名
         * @param value 列名对应的值
         * Created by jinphy, on 2018/1/3, at 16:12
         */
        Operate whereBt(String column, Object value);

        /**
         * DESC: wheres 大于等于条件，通过该方法查询的条件为 >= ，例如 account >= 123
         *      该方法可以调用多次
         *
         * @param column 指定条件的列名
         * @param value 列名对应的值
         * Created by jinphy, on 2018/1/3, at 16:12
         */
        Operate whereBe(String column, Object value);


        /**
         * DESC: wheres 不等于条件，通过该方法查询的条件为 <> ，例如 account <> 123
         *      该方法可以调用多次
         *
         * @param column 指定条件的列名
         * @param value 列名对应的值
         * Created by jinphy, on 2018/1/3, at 16:12
         */
        Operate whereLB(String column, Object value);

        /**
         * DESC: wheres like条件，通过该方法查询的条件为 like ，例如 account like 123
         *      该方法可以调用多次
         *
         * @param column 指定条件的列名
         * @param value 列名对应的值
         * Created by jinphy, on 2018/1/3, at 16:12
         */
        Operate whereLike(String column, Object value);

        /**
         * DESC: wheres between条件，通过该方法查询的条件为 between ，例如 id between 1 and 2
         *      该方法可以调用多次
         *
         * @param column 指定条件的列名
         * @param from 列名对应的开始值
         * @param to 列名对应的结束值
         * Created by jinphy, on 2018/1/3, at 16:12
         */
        Operate whereBetween(String column, Object from, Object to);

        /**
         * DESC: wheres in条件，通过该方法查询的条件为 in ，例如 id in (1, 2)
         *      该方法可以调用多次
         *
         * @param column 指定条件的列名
         * @param values 列名对应的值得集合
         * Created by jinphy, on 2018/1/3, at 16:12
         */
        Operate whereIn(String column, Object...values);


        /**
         * DESC: wheres not like条件，通过该方法查询的条件为 not like ，例如 account not like 123
         *      该方法可以调用多次
         *
         * @param column 指定条件的列名
         * @param value 列名对应的值
         * Created by jinphy, on 2018/1/3, at 16:12
         */
        Operate whereNotLike(String column, Object value);

        /**
         * DESC: wheres not between条件，通过该方法查询的条件为 not between ，例如 id not between 1 and 2
         *      该方法可以调用多次
         *
         * @param column 指定条件的列名
         * @param from 列名对应的开始值
         * @param to 列名对应的结束值
         * Created by jinphy, on 2018/1/3, at 16:12
         */
        Operate whereNotBetween(String column, Object from, Object to);

        /**
         * DESC: wheres not in条件，通过该方法查询的条件为 not in ，例如 id not in (1, 2)
         *      该方法可以调用多次
         *
         * @param column 指定条件的列名
         * @param values 列名对应的值得集合
         * Created by jinphy, on 2018/1/3, at 16:12
         */
        Operate whereNotIn(String column, Object...values);

        /**
         * DESC: 运行外部代码，该方法传入的接口会立刻执行，并且返回一个Boolean类型的结果
         *
         *      注意：如果返回true则表示可以继续执行其他操作，
         *           如果返回false则表示条件不符合，该方法执行后将不会执行数据库操作，
         *           并且generateSql() 方法返回null
         *
         *      说明：这个方法的作用是什么呢，在链式调用的时候，如果要插入的数据有很多条，那么
         *      就可以用该方法根据接口中传入的operate对象循环执行方法columnValues()；
         *      或者可以在该方法中执行新的数据库插入操作，然后在根据查询结果决定是否执行当前
         *      数据库操作（也可以把潜逃的数据库操作结果用作外层数据库操作的条件传给外层数据库）
         * Created by jinphy, on 2018/1/4, at 12:10
         */
        Operate run(Function<Operate, Boolean> run);

        String generateSql();

        Result execute();
    }


    public interface Function<T,V>{
        V apply(T value) throws Exception;
    }

}
