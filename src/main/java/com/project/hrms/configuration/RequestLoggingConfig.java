package com.project.hrms.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
@Configuration
public class RequestLoggingConfig {

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        System.out.println(">>> RequestLoggingFilter LOADED");  // log này phải xuất hiện khi app start

        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludePayload(true);
        loggingFilter.setIncludeHeaders(true);
        loggingFilter.setMaxPayloadLength(10000);
        loggingFilter.setAfterMessagePrefix("REQUEST BODY: ");
        return loggingFilter;
    }
}
