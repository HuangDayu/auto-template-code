package com.tenny.autocode.database;

import java.util.List;
import java.util.Map;

/**
 * @author huangdayu create at 2021/11/23 12:25
 */
public interface DatabaseService {

    /**
     * 连接是否有效
     *
     * @param timeout
     * @return
     */
    boolean isValid(int timeout);

    /**
     * 获取所有表
     *
     * @return
     */
    List<String> getTables();

    /**
     * 获取表的所有列
     *
     * @param tableName
     * @return
     */
    List<Map<String, String>> getTableColumns(String tableName);

}
