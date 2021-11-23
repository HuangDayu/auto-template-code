package com.tenny.autocode.database;

import java.util.List;
import java.util.Map;

/**
 * @author huangdayu create at 2021/11/23 12:25
 */
public interface DatabaseService {
    List<String> getTables();

    List<Map<String, String>> getTableCloumns(String tableName);
}
