package com.haidian.intel.platform.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置。
 * 当前阶段使用 SpringDoc + Knife4j 输出接口文档，方便后续前后端联调和接口确认。
 */
@Configuration
public class OpenApiConfig {

    /**
     * Build the OpenAPI description used by SpringDoc and Knife4j.
     *
     * @return OpenAPI metadata
     */
    @Bean
    public OpenAPI haidianIntelOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("海淀区情报平台后端接口")
                        .description("海淀区情报平台当前后端交付版本接口文档")
                        .version("v1.0.0")
                        .contact(new Contact().name("Codex")));
    }
}
