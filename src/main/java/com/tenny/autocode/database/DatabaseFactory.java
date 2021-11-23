package com.tenny.autocode.database;

import com.tenny.autocode.util.MysqlUtil;
import com.tenny.autocode.util.OracleUtil;
import com.tenny.autocode.util.PostgresqlUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.tenny.autocode.util.ParamUtil.isEmpty;

/**
 * @author huangdayu create at 2021/11/23 12:31
 */
public class DatabaseFactory {

    private static final Map<String, DatabaseService> databases = new ConcurrentHashMap<>();

    public static DatabaseService getDatabaseService(String type, String url, String userName, String passWord) {
        if ("Postgresql".equalsIgnoreCase(type)) {
            return databases.computeIfAbsent(url, s -> new PostgresqlUtil(url, userName, passWord));
        }
        if ("Mysql".equalsIgnoreCase(type)) {
            return databases.computeIfAbsent(url, s -> new MysqlUtil(url, userName, passWord));
        }
        if ("Oracle".equalsIgnoreCase(type)) {
            return databases.computeIfAbsent(url, s -> new OracleUtil(url, userName, passWord));
        }
        return null;
    }

}
