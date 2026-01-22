package com.supercode.framework.config.openapi;

import com.supercode.master.utils.json.JacksonUtil;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author jonathan.ji
 */
@Configuration
@ConditionalOnWebApplication
public class SwaggerConfig {
    private static final Logger log = LoggerFactory.getLogger(SwaggerConfig.class);

    @Value("${swagger.package.scan:}")
    private String packageScanConfigPath;

    private static String generateuuid() {
        String s = UUID.randomUUID().toString();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            builder.append((c == '-') ? "" : c);
        }
        return builder.toString();
    }

    @Bean
    public OpenApiCustomizer customerGlobalHeaderOpenApiCustomizer() {
        return openApi -> {
            Paths paths = openApi.getPaths();
            Collection<PathItem> values = paths.values();
            for (PathItem pathItem : values) {
                List<Operation> operations = pathItem.readOperations();
                for (Operation operation : operations) {
                    buildRequestHeader(operation);
                }
            }
        };
    }

    private void buildRequestHeader(Operation operation) {
        HeaderParameter userIdHeader = new HeaderParameter();
        userIdHeader.setName("x-user-id");
        userIdHeader.setDescription("user id");
        userIdHeader.setAllowEmptyValue(true);
        userIdHeader.setRequired(false);
        userIdHeader.setSchema(new StringSchema()._default(""));
        operation.addParametersItem(userIdHeader);

        HeaderParameter grayHeader = new HeaderParameter();
        grayHeader.setName("x-gray-env");
        grayHeader.setDescription("env flag");
        grayHeader.setAllowEmptyValue(true);
        grayHeader.setRequired(false);
        grayHeader.setSchema(new StringSchema()._default("normal"));
        operation.addParametersItem(grayHeader);

        HeaderParameter traceHeader = new HeaderParameter();
        traceHeader.setName("x-trace-id");
        traceHeader.setDescription("x-trace-id");
        traceHeader.setAllowEmptyValue(true);
        traceHeader.setRequired(false);
        traceHeader.setSchema(new StringSchema()._default(generateuuid()));
        operation.addParametersItem(traceHeader);
    }

    @Bean
    public GroupedOpenApi adminApi(OpenApiCustomizer customerGlobalHeaderOpenApiCustomizer) {
        long startTime = System.currentTimeMillis();

        String[] packagePaths;
        if (StringUtils.isNotBlank(packageScanConfigPath)) {
            packagePaths = packageScanConfigPath.split(",");
        } else {
            packagePaths = getRestControllerPackagePaths();
            log.info("swagger config packagePaths:{}", JacksonUtil.toJsonStr(packagePaths));
        }

        if (System.currentTimeMillis() - startTime > 1000L) {
            log.warn("swagger config time cost:{}", System.currentTimeMillis() - startTime);
        }
        return GroupedOpenApi.builder()
                .group("happy-tree restful endpoint")
                .packagesToScan(packagePaths)
                .addOpenApiCustomizer(customerGlobalHeaderOpenApiCustomizer)
                .build();
    }

    public String[] setToStringArray(Set<String> set) {
        String[] array = new String[set.size()];
        int index = 0;
        for (String element : set) {
            array[index++] = element;
        }
        return array;
    }

    public String[] getRestControllerPackagePaths() {
        Set<String> packagePaths = new HashSet<>();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        // 设置你的扫描包路径
        Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents("com.happytree");
        for (BeanDefinition beanDefinition : beanDefinitions) {
            String packagePath = beanDefinition.getBeanClassName()
                    .substring(0, beanDefinition.getBeanClassName().lastIndexOf('.'));
            packagePaths.add(packagePath);
        }
        return this.setToStringArray(packagePaths);
    }
}
