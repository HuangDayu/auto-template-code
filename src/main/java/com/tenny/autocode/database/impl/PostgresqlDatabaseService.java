package com.tenny.autocode.database.impl;

import com.tenny.autocode.database.DatabaseService;
import com.tenny.autocode.util.ParamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostgresqlDatabaseService implements DatabaseService {
    private static final Logger log = LoggerFactory.getLogger(PostgresqlDatabaseService.class);

    private String username = "postgres";
    private static Connection connection = null;

    /**
     * 初始化数据库链接
     *
     * @param url      数据库地址
     * @param username 用户名
     * @param password 密码
     */
    public PostgresqlDatabaseService(String url, String username, String password) {
        try {
            this.username = username;
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(String.format("jdbc:postgresql://%s", url), username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isValid(int timeout) {
        try {
            return connection != null && connection.isValid(timeout);
        } catch (SQLException throwables) {
            log.error("Postgresql Database connection error ", throwables);
            return false;
        }
    }

    /**
     * 获取当前用户所有表
     *
     * @return
     */
    @Override
    public List<String> getTables() {
        List<String> tables = new ArrayList<>();

        try {
            Statement stmt = connection.createStatement();
            String sql = "SELECT tablename FROM pg_tables WHERE tablename NOT LIKE 'pg%' AND tablename NOT LIKE 'sql_%' ORDER BY tablename";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                tables.add(rs.getString("tablename"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tables;
    }

    /**
     * 获取表字段信息
     *
     * @param tableName
     * @return
     */
    @Override
    public List<Map<String, String>> getTableColumns(String tableName) {
        List<Map<String, String>> columns = new ArrayList<Map<String, String>>();
        try {
            Statement stmt = connection.createStatement();

            String sql = "select column_name, data_type, column_default, is_nullable from information_schema.columns where table_name='" + tableName + "'";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("columnName", rs.getString("column_name"));
                map.put("dataType", rs.getString("data_type"));
                map.put("isKey", ParamUtil.isEmpty(rs.getString("column_default")) ? "false" : "true");
                map.put("notNull", rs.getString("is_nullable").equals("YES") ? "false" : "true");
                map.put("comment", rs.getString("column_name"));
                columns.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (null != connection) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return columns;
    }

}
