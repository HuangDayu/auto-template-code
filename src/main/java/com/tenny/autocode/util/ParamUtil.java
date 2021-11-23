package com.tenny.autocode.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tenny.autocode.database.DatabaseFactory;
import com.tenny.autocode.database.DatabaseService;
import com.tenny.autocode.entity.CodeEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParamUtil {

    private static final Logger log = LoggerFactory.getLogger(FreemarkerUtil.class);

    // 数据库类型转属性类型
    public static Map<String, String> dataTypeMap;

    static {
        dataTypeMap = new HashMap<>();
        dataTypeMap.put("NUMBER", "Integer");
        dataTypeMap.put("NVARCHAR2", "String");
        dataTypeMap.put("TIMESTAMP", "Date");
        dataTypeMap.put("TIMESTAMP(6)", "Date");

        dataTypeMap.put("character varying", "String");
        dataTypeMap.put("integer", "Integer");
        dataTypeMap.put("smallint", "Integer");
        dataTypeMap.put("date", "Date");
        dataTypeMap.put("timestamp", "Date");
        dataTypeMap.put("timestamp without time zone", "Date");
        dataTypeMap.put("boolean", "Boolean");
        dataTypeMap.put("numeric", "BigDecimal");

        dataTypeMap.put("text", "String");
        dataTypeMap.put("varchar", "String");
        dataTypeMap.put("char", "String");
        dataTypeMap.put("int", "Integer");
        dataTypeMap.put("datetime", "Date");
    }

    public static Map<String, Object> getFinalParamFromPage(CodeEntity entity) {
        Map<String, Object> beanMap = new HashMap<String, Object>();
        beanMap.put("tableName", entity.getEntityName());// 数据库表名
        beanMap.put("entityName", entity.getEntityName());// 实体类名

        List<Map<String, String>> fieldList = entity.getFieldList();
        for (Map<String, String> field : fieldList) {
            field.put("columnName", field.get("fieldName"));// 数据库字段名
            // The only legal comparisons are between two numbers, two strings, or two dates.
            field.put("isKey", String.valueOf(field.get("isKey")));// true-->"true", false-->"false"
            field.put("notNull", String.valueOf(field.get("notNull")));// 同上
        }

        beanMap.put("params", fieldList);
        return beanMap;
    }

    /**
     * 拼装最终参数(从数据库解析字段)
     *
     * @return
     */
    public static Map<String, Object> getFinalParamFromDB(CodeEntity entity) {
        Map<String, Object> beanMap = new HashMap<String, Object>();
        beanMap.put("tableName", entity.getDbTable());// 数据库表名
        beanMap.put("entityName", entity.getEntityName());// 实体类名

        List<Map<String, String>> paramsList = new ArrayList<>();
        List<Map<String, String>> columnList = null;
        DatabaseService databaseService = DatabaseFactory.getDatabaseService(entity.getDbType(), entity.getDbUrl(), entity.getDbUser(), entity.getDbPw());
        if (databaseService != null) {
            columnList = databaseService.getTableColumns(entity.getDbTable());
        }
        for (Map<String, String> column : columnList) {
            Map<String, String> param = new HashMap<String, String>();
            param.put("columnName", column.get("columnName"));// 数据库字段名
            param.put("fieldName", column2Property(column.get("columnName")));// 实体属性名
            param.put("fieldType", columnType2FieldType(column.get("dataType")));// 实体属性类型
            param.put("fieldNote", column.get("comment"));// 字段说明，即属性说明
            param.put("notNull", column.get("notNull"));// 非空
            param.put("isKey", column.get("isKey"));// 是否主键
            paramsList.add(param);
        }

        beanMap.put("params", paramsList);
        return beanMap;
    }


    // 数据库字段转属性【eg：USER_ID --> userId】
    public static String column2Property(String fieldName) {
        StringBuffer result = new StringBuffer();

        fieldName = fieldName.toLowerCase();
        String[] fields = fieldName.split("_");
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            if (i == 0) {
                result.append(field);
            } else {
                result.append(toUpString(field));
            }
        }

        return result.toString();
    }

    // 首字母大写【eg：user --> User】
    public static String toUpString(String className) {
        char[] cs = className.toCharArray();
        cs[0] -= 32;
        String ClassName = String.valueOf(cs);
        return ClassName;
    }

    // 首字母小写【eg：User --> user】
    public static String toLowString(String className) {
        char[] cs = className.toCharArray();
        cs[0] += 32;
        String ClassName = String.valueOf(cs);
        return ClassName;
    }

    // 数据库类型转属性类型【eg: NVARCHAR2 --> String】
    public static String columnType2FieldType(String columnType) {
        String type = dataTypeMap.get(columnType);
        if (type == null) {
            log.error("column {} type is null !", columnType);
            return "String";
        }
        return type;
    }

    public static boolean isEmpty(String value) {
        if (null == value || value.equals("")) {
            return true;
        }
        return false;
    }

}
