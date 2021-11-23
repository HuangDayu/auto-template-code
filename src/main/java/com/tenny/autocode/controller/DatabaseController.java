package com.tenny.autocode.controller;

import static com.tenny.autocode.util.ParamUtil.isEmpty;

import com.tenny.autocode.database.DatabaseFactory;
import com.tenny.autocode.database.DatabaseService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tenny.autocode.common.Result;
import com.tenny.autocode.entity.CodeEntity;

@RestController
@RequestMapping("db")
public class DatabaseController {

    @RequestMapping("getTables")
    public Result getTables(CodeEntity entity) {
        Result result = new Result();
        // 从数据库获取表
        DatabaseService databaseService = DatabaseFactory.getDatabaseService(entity.getDbType(), entity.getDbUrl(), entity.getDbUser(), entity.getDbPw());
        if (databaseService != null) {
            result.setData(databaseService.getTables());
        }
        return result;
    }

}
