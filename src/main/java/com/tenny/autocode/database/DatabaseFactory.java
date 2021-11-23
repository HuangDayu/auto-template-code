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
        DatabaseService databaseService = null;
        if ("Postgresql".equalsIgnoreCase(type)) {
            databaseService = databases.computeIfAbsent(url, s -> new PostgresqlDatabaseService(url, username, password));
        } else if ("Mysql".equalsIgnoreCase(type)) {
            databaseService = databases.computeIfAbsent(url, s -> new MysqlDatabaseService(url, username, password));
        } else if ("Oracle".equalsIgnoreCase(type)) {
            databaseService = databases.computeIfAbsent(url, s -> new OracleDatabaseService(url, username, password));
        }
        return databaseService != null && databaseService.isValid(10) ? databaseService : null;
    }

}
