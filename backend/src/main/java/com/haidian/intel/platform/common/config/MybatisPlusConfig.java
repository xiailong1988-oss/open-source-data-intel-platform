package com.haidian.intel.platform.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 基础配置。
 * 当前阶段先接入分页拦截器，为后续地区、情报等分页查询接口预留统一能力。
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * Register the MyBatis-Plus interceptor used by current paging queries.
     *
     * @return MyBatis-Plus interceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
