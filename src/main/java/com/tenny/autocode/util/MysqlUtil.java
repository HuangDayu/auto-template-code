package com.tenny.autocode.util;

import com.tenny.autocode.database.DatabaseService;

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

public class MysqlUtil implements DatabaseService {
    private static String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static String DATABASE_USER = "root";
    private static Connection connection = null;
    private static DatabaseMetaData databaseMetaData = null;

    /**
     * 初始化数据库链接
     *
     * @param db_url  数据库地址
     * @param db_user 用户名
     * @param db_pw   密码
     */
    public MysqlUtil(String db_url, String db_user, String db_pw) {
        try {
            DATABASE_USER = db_user;
            Class.forName(DRIVER_CLASS);
            connection = DriverManager.getConnection(String.format("jdbc:mysql://%s?serverTimezone=Asia/Shanghai", db_url), db_user, db_pw);
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
            ResultSet rs = databaseMetaData.getTables(null, DATABASE_USER, null, new String[]{"TABLE"});
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
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
    public List<Map<String, String>> getTableCloumns(String tableName) {
        List<Map<String, String>> columns = new ArrayList<Map<String, String>>();
        try {
            Statement stmt = connection.createStatement();

            String sql = "select column_name, data_type, column_key, is_nullable, column_comment from information_schema.columns where table_name='" + tableName + "'";
            ResultSet rs = stmt.executeQuery(sql);
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
