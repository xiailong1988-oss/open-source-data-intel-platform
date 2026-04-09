package com.haidian.intel.platform.common.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.TimeZone;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 统一 Jackson 配置，保证时间字段按上海时区和固定格式输出。
 */
@Configuration
public class JacksonConfig {

    /**
     * Customize the global Jackson builder so every interface keeps the same time zone and date format.
     *
     * @return Jackson builder customizer
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            builder.timeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            builder.simpleDateFormat("yyyy-MM-dd HH:mm:ss");
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }
}
