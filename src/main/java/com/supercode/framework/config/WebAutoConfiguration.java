package com.supercode.framework.config;

import com.supercode.framework.filter.TracingOncePerRequestFilter;
import com.supercode.master.utils.platform.FilterOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: jonathan.ji
 * @Date: 2022/3/6 16:17
 * @Desc: Web自动装配
 */
@Configuration
@ConditionalOnWebApplication
public class WebAutoConfiguration {

    @Bean
    public TracingOncePerRequestFilter tracingOncePerRequestFilter() {
        return new TracingOncePerRequestFilter();
    }

    @Bean
    public FilterRegistrationBean<TracingOncePerRequestFilter> tracingOncePerRequestFilterBean(TracingOncePerRequestFilter tracingOncePerRequestFilter) {
        FilterRegistrationBean<TracingOncePerRequestFilter> ret = new FilterRegistrationBean<>();
        ret.setFilter(tracingOncePerRequestFilter);
        ret.addUrlPatterns("/*");
        ret.setOrder(FilterOrder.FILTERORDER_1);
        return ret;
    }
}
