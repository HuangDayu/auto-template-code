package com.tenny.autocode.util;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

import com.tenny.autocode.entity.CodeEntity;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.utility.StringUtil;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class FreemarkerUtil {
    private FreemarkerUtil() {
    }

    private static final Logger log = LoggerFactory.getLogger(FreemarkerUtil.class);

    private static final String MODEL_SUFFIX = ".ftl";

    // 存储所有模板名
    private static final List<String> modelNameList = Arrays.asList("service", "serviceImpl", "dao", "mapping", "entity", "controller");

    // 存储结果需要转换【StringUtil.XMLEnc】的模板名
    private static final List<String> needTransferNameList = Collections.singletonList("mapping");

    // 工具类配置
    private static final String DECODE = "UTF-8";

    private static final Configuration config = new Configuration();

    private static final StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();

    private static Template template;

    // 所有模板的字符串
    private static Map<String, String> templateMap;

    public static void init() {
        templateMap = readTemplate();
        for (Entry<String, String> entry : templateMap.entrySet()) {
            stringTemplateLoader.putTemplate(entry.getKey(), entry.getValue());
        }
        config.setTemplateLoader(stringTemplateLoader);
    }

    /**
     * 产生所有业务类(来源为表单输入)
     *
     * @param entity 实体类
     */
    public static Map<String, String> getAllClassFromPage(CodeEntity entity) {
        return getAllClass(ParamUtil.getFinalParamFromPage(entity));
    }

    /**
     * 产生所有业务类(来源为数据库)
     *
     * @param entity 实体类
     */
    public static Map<String, String> getAllClassFromDB(CodeEntity entity) {
        return getAllClass(ParamUtil.getFinalParamFromDB(entity));
    }

    /**
     * 产生所有业务类
     *
     * @param beanMap 属性集合
     * @return Map<String, String> 业务类集合
     */
    private static Map<String, String> getAllClass(Map<String, Object> beanMap) {
        Map<String, String> bzClassMap = new HashMap<>();

        String entityName = (String) beanMap.get("entityName");

        for (String modelName : modelNameList) {
            if (needTransferNameList.contains(modelName)) {
                bzClassMap.put(modelName, StringUtil.XMLEnc(templateToClass(modelName, modelName + " of " + entityName, beanMap)));
            } else {
                bzClassMap.put(modelName, templateToClass(modelName, modelName + " of " + entityName, beanMap));
            }
        }
        return bzClassMap;
    }

    /**
     * 产生java类
     *
     * @param template 模板字符串名【eg：entityTemplate】
     * @param target   java类名【eg：User】，仅打印调试用，实际产生结果为字符串
     * @param rootMap  参数，包括接口名，实体名，实体属性等
     * @return String 业务类
     */
    private static String templateToClass(String template, String target, Object rootMap) {
        StringWriter stringWriter = new StringWriter();
        try {
            FreemarkerUtil.template = config.getTemplate(template, DECODE);
            Writer writer = new BufferedWriter(stringWriter);
            FreemarkerUtil.template.process(rootMap, writer);
            writer.flush();
            writer.close();
            log.info("Freemarker generate '{}' code is already ok", target);
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
        return stringWriter.toString();
    }

    /**
     * 将所有.ftl文件内容转换为字符串
     *
     * @return
     */
    private static Map<String, String> readTemplate() {
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        assert url != null;
        if (url.getProtocol().equals("jar")) {
            return readTemplateForJar();
        }
        Map<String, String> fileStrMap = new HashMap<>();
        String path = url.toString().replace("file:/", "")
                .replace("/", File.separator)
                .concat(File.separator)
                .concat("templates")
                .concat(File.separator);
        for (String modelName : modelNameList) {
            fileStrMap.put(modelName, readTemplateForProject(path + modelName + MODEL_SUFFIX));
        }
        return fileStrMap;
    }

    private static Map<String, String> readTemplateForJar() {
        Map<String, String> fileStrMap = new HashMap<>();
        String resourcesPath = "classpath:**/*" + MODEL_SUFFIX;
        String line;
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(resourcesPath);
            log.info("Freemarker {} resource size {}", resourcesPath, resources.length);
            for (Resource resource : resources) {
                String description = resource.getDescription();
                description = description.substring(description.indexOf('[') + 1, description.indexOf(']'));
                log.debug("description {} name {}", resource.getDescription(), description);
                if (description.endsWith("/") || description.endsWith(".class")) {
                    continue;
                }
                StringBuilder buffer = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line).append(System.getProperty("line.separator"));// 保持原有换行格式
                }
                bufferedReader.close();
                IOUtils.closeQuietly(resource.getInputStream());
                log.debug("Read Freemarker file {} size {}", description, buffer.length());
                for (String modelName : modelNameList) {
                    if (description.endsWith(modelName + MODEL_SUFFIX)) {
                        log.debug("Add Freemarker file to buffer string {}", description);
                        fileStrMap.put(modelName, buffer.toString());
                    }
                }
            }
        } catch (IOException e) {
            log.error("Create temp file error ", e);
        }
        return fileStrMap;
    }

    /**
     * 将模板内容转成字符串
     *
     * @param filePath 模板名【eg：entity.ftl】
     * @return
     */
    private static String readTemplateForProject(String filePath) {
        StringBuilder stringBuffer = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line).append(System.getProperty("line.separator"));// 保持原有换行格式
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

    /**
     * 获取所有模板【为前端能正常显示，部分特殊符号需转换，如"<"转为"&lt;"】
     *
     * @return
     */
    public static Map<String, String> getAllTemplate() {
        Map<String, String> templateStrMap = new HashMap<>();
        for (String modelName : modelNameList) {
            if (needTransferNameList.contains(modelName)) {
                templateStrMap.put(modelName, StringUtil.XMLEnc(templateMap.get(modelName)));
            } else {
                templateStrMap.put(modelName, templateMap.get(modelName));
            }
        }
        return templateStrMap;
    }
}
