package com.supercode.framework;

import com.supercode.framework.log.layout.LogMaskWordUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class LogMaskUtilsTestUtils {

    @Test
    public void maskJsonString() {
        LogMaskWordUtil.init(Arrays.asList("myPhone", "email#"));
        String ret = LogMaskWordUtil.fastMaskJsonStr("{'myPhone':20398420,'email':'test@xxx.com','isValid':'false'}");
        assertEquals("{\"myPhone\":\"***\",\"email\":\"##>E0B97E6ACDFC40C5389A0BD15888F29D\",\"isValid\":\"false\"}", ret);
    }
}
