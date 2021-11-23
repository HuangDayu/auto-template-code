package com.tenny.autocode.database.impl;

import com.tenny.autocode.database.DatabaseService;
import com.tenny.autocode.util.ParamUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MysqlDatabaseService implements DatabaseService {
    private String username = "root";
    private Connection connection = null;
    private DatabaseMetaData databaseMetaData = null;

    /**
     * 初始化数据库链接
     *
     * @param url      数据库地址，例如：localhost:3306/news
     * @param username 用户名
     * @param password 密码
     */
    public MysqlDatabaseService(String url, String username, String password) {
        try {
            this.username = username;
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(String.format("jdbc:mysql://%s?serverTimezone=Asia/Shanghai", url), username, password);
            databaseMetaData = connection.getMetaData();
        } catch (Exception e) {
            e.printStackTrace();
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
            ResultSet resultSet = databaseMetaData.getTables(null, username, null, new String[]{"TABLE"});
            while (resultSet.next()) {
                tables.add(resultSet.getString("TABLE_NAME"));
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
            Statement statement = connection.createStatement();

            String sql = "select column_name, data_type, column_key, is_nullable, column_comment from information_schema.columns where table_name='" + tableName + "'";
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("columnName", rs.getString("column_name"));
                // TODO .dataType转换【varchar-->String，并将此转换由paramutil移致mysqlUtil及oracleUtil内部，使得产生的columns直接可用】
                map.put("dataType", rs.getString("data_type"));
                map.put("isKey", ParamUtil.isEmpty(rs.getString("column_key")) ? "false" : "true");
                map.put("notNull", rs.getString("is_nullable").equals("YES") ? "false" : "true");
                map.put("comment", rs.getString("column_comment"));
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