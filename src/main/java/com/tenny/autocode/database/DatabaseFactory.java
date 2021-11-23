package com.tenny.autocode.database;

import com.tenny.autocode.database.impl.MysqlDatabaseService;
import com.tenny.autocode.database.impl.OracleDatabaseService;
import com.tenny.autocode.database.impl.PostgresqlDatabaseService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author huangdayu create at 2021/11/23 12:31
 */
public class DatabaseFactory {
    private DatabaseFactory() {
    }

    private static final Map<String, DatabaseService> databases = new ConcurrentHashMap<>();

    public static DatabaseService getDatabaseService(String type, String url, String username, String password) {
        if ("Postgresql".equalsIgnoreCase(type)) {
            return databases.computeIfAbsent(url, s -> new PostgresqlDatabaseService(url, username, password));
        }
        if ("Mysql".equalsIgnoreCase(type)) {
            return databases.computeIfAbsent(url, s -> new MysqlDatabaseService(url, username, password));
        }
        if ("Oracle".equalsIgnoreCase(type)) {
            return databases.computeIfAbsent(url, s -> new OracleDatabaseService(url, username, password));
        }
        return null;
    }

}
