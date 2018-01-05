package com.jinphy.simplechatserver.constants;

import com.jinphy.simplechatserver.utils.EncryptUtils;

/**
 * Created by jinphy on 2017/8/9.
 */

public class StringConst {

    public static final String PREFERENCES_NAME_USER = EncryptUtils.md5("user");
    public static final String PREFERENCES_KEY_REMEMBER_PASSWORD = EncryptUtils.md5("remember_password");
    public static final String PREFERENCES_KEY_CURRENT_ACCOUNT = EncryptUtils.md5("current_account");
    public static final String PREFERENCES_KEY_HAS_LOGIN = EncryptUtils.md5("has_login");
    public static final String PREFERENCES_KEY_PASSWORD = "password";

    public static final String MD5 = "md5";

    public static final String UTF_8 = "UTF-8";

    public static final String POST = "POST";

    public static final String GET = "GET";


    public static final String INSERT = "INSERT";
    public static final String UPDATE = "UPDATE";
    public static final String SELECT = "SELECT";
    public static final String DELETE = "DELETE";

    public static final String FROM = "FROM";

    public static final String INTO = "INTO";


    public static final String LIKE = "LIKE";

    public static final String BETWEEN = "BETWEEN";

    public static final String IN = "IN";

    public static final String AND = "AND";

    public static final String WHERE = "WHERE";

    public static final String HAVING = "HAVING";

    public static final String GROUP_BY = "GROUP BY";

    public static final String ORDER_BY = "ORDER BY";

    public static final String VALUES = "VALUES";

    public static final String SET = "SET";

    public static final String LIMIT = "LIMIT";


    public static final String COMMA = ",";

    public static final String DOT = ".";

    public static final String EQ = "=";

    public static final String LT = "<";

    public static final String LE = "<=";

    public static final String BT = ">";

    public static final String BE = ">=";

    public static final String LB = "<>";

    public static final String NE = "!=";

    public static final String PLUS = "+";

    public static final String ASTERISK = "*";

    public static final String UNDER_LINE = "_";

    public static final String SLASH = "/";

    public static final String LEFT_S_BRACKET = "(";

    public static final String RIGHT_S_BRACKET = ")";


    public static final String LEFT_M_BRACKET = "[";

    public static final String RIGHT_M_BRACKET = "]";

    public static final String LEFT_B_BRACKET = "{";

    public static final String RIGHT_B_BRACKET = "}";

    public static final String BLANK = " ";

    public static final String LINE = "\n";

    public static final String NOT = "NOT";
    public static final String SINGLE_QUOTATION_MARK = "'";

    public static final String DOUBLE_QUOTATION_MARK = "\"";


    public static final String ASC = "ASC";       // 升序

    public static final String DESC = "DESC";     // 降序


    public static final String FALSE = "FALSE";

    public static final String TRUE = "TRUE";
}
