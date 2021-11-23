package com.tenny.autocode.init;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.tenny.autocode.util.FreemarkerUtil;

@Component
public class BaseDataInitializer implements ApplicationRunner {

    Logger log = LoggerFactory.getLogger(BaseDataInitializer.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 初始化模板资源到内存
        FreemarkerUtil.init();
        log.info("Init templates success.");
    }

}
