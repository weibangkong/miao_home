package com.miaohome.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger 接口文档配置
 * <p>基于 springdoc-openapi 自动扫描 Controller 生成文档，
 * 访问地址：{context-path}/swagger-ui/index.html。</p>
 * <p>通过 {@link OperationCustomizer} 为所有接口自动补充 X-Tenant-Id 请求头说明。</p>
 *
 * @author weibang kong
 */
@Configuration
public class OpenApiConfig {

    /**
     * 构建 OpenAPI 文档基本信息
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("猫咪之家后端接口文档")
                        .description("小区流浪猫记录管理系统 API")
                        .version("1.0.0"));
    }


    /**
     * 全局请求头定制
     * <p>为所有接口统一追加多租户隔离所需的 X-Tenant-Id 请求头，
     * 认证类接口（注册/登录）该请求头可忽略。</p>
     */
    @Bean
    public OperationCustomizer tenantHeaderCustomizer() {
        return (operation, handlerMethod) -> operation.addParametersItem(
                new Parameter()
                        .in("header")
                        .name("X-Tenant-Id")
                        .required(false)
                        .description("租户 ID，用于多租户数据隔离（默认 1）")
                        .schema(new StringSchema()));
    }
}
