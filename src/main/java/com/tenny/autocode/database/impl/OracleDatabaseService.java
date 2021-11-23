package com.tenny.autocode.database.impl;

import com.tenny.autocode.database.DatabaseService;
import com.tenny.autocode.util.FreemarkerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class OracleDatabaseService implements DatabaseService {
    private static final Logger log = LoggerFactory.getLogger(OracleDatabaseService.class);

    private String username = "root";
    private Connection connection = null;
    private DatabaseMetaData databaseMetaData = null;

    /**
     * 初始化数据库链接
     *
     * @param url      数据库地址，例如：192.168.2.90:1521:orcl
     * @param username 用户名
     * @param password 密码
     */
    public OracleDatabaseService(String url, String username, String password) {
        try {
            this.username = username;
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection(String.format("jdbc:oracle:thin:@%s", url), username, password);
            databaseMetaData = connection.getMetaData();
            log.info("Create Database Connection {} success", connection.getMetaData().getURL());
        } catch (Exception e) {
            log.error("Create Database Connection {} failure , username {} , password {} , message {}", url, username, password, e.getMessage());
        }
    }

    @Override
    public boolean isValid(int timeout) {
        try {
            return connection != null && connection.isValid(timeout);
        } catch (SQLException throwables) {
            log.error("Oracle Database connection error ", throwables);
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
        List<String> tables = new ArrayList<String>();

        try {
            ResultSet rs = databaseMetaData.getTables("null", username.toUpperCase(), "%", new String[]{"TABLE"});
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
    public List<String> getColumns(String tableName) {
        List<String> columns = new ArrayList<>();
        try {
            ResultSet rs = databaseMetaData.getColumns(null, "%", tableName, "%");
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return columns;
    }

    /**
     * 获取表字段
     *
     * @param Table
     * @return
     */
    @Override
    public List<Map<String, String>> getTableColumns(String Table) {

        List<Map<String, String>> columns = new ArrayList<Map<String, String>>();

        try {
            Statement stmt = connection.createStatement();

            String sql =
                    "select " +
                            "         comments as \"comments\"," +
                            "         a.COLUMN_NAME \"columnName\"," +
                            "         a.DATA_TYPE as \"dataType\"," +
                            "         b.comments as \"comment\"," +
                            "         decode(c.column_name,null,'false','true') as \"isKey\"," +
                            "         decode(a.NULLABLE,'N','true','Y','false','') as \"notNull\"," +
                            "         '' \"sequence\"" +
                            "   from " +
                            "       all_tab_columns a, " +
                            "       all_col_comments b," +
                            "       (" +
                            "        select a.constraint_name, a.column_name" +
                            "          from user_cons_columns a, user_constraints b" +
                            "         where a.constraint_name = b.constraint_name" +
                            "               and b.constraint_type = 'P'" +
                            "               and a.table_name = '" + Table + "'" +
                            "       ) c" +
                            "   where " +
                            "     a.Table_Name=b.table_Name " +
                            "     and a.column_name=b.column_name" +
                            "     and a.Table_Name='" + Table + "'" +
                            "     and a.owner=b.owner " +
                            "     and a.owner='" + username.toUpperCase() + "'" +
                            "     and a.COLUMN_NAME = c.column_name(+)" +
                            "  order by a.COLUMN_ID";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("comments", rs.getString("comments"));
                map.put("columnName", rs.getString("columnName"));
                map.put("dataType", rs.getString("dataType"));
                map.put("comment", rs.getString("comment"));
                map.put("isKey", rs.getString("isKey"));
                map.put("notNull", rs.getString("notNull"));
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
