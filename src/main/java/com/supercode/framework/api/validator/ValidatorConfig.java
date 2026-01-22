package com.supercode.framework.api.validator;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidatorConfig {

    @Resource
    private Validator validator;

    @PostConstruct
    private void init() {
        ValidatorUtils.init(validator);
    }
}
