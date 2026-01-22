package com.supercode.framework.filter;

import com.supercode.master.env.EnvUtil;
import com.supercode.master.utils.platform.RpcContext;
import com.supercode.master.utils.platform.TrackingUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @Author: jonathan.ji
 * @Date: 2022/1/23 21:58
 * @Desc:
 */
public class TracingOncePerRequestFilter extends OncePerRequestFilter {

    public static final String HEADER_ENCODED_HEADER_NAME = "x-header-encoded";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, jakarta.servlet.FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String traceId = TrackingUtil.getTraceIdFromRequest(request);
            logger.debug(String.format("receive traceId from upstream services and the traceId is: %s", traceId));
            traceId = decodeHeaderValue(request, traceId);
            TrackingUtil.saveTrace(traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
            RpcContext.clear();
        }
    }

    /**
     * 这个逻辑需要与如下方法保持一致
     * com.supercode.platform.openfeign.body.CustomeHeaderServletRequestWrapper#isHeaderEncoded()
     */
    private boolean isHeaderEncoded(HttpServletRequest request) {
        String value = request.getHeader(HEADER_ENCODED_HEADER_NAME);
        return StringUtils.isNotBlank(value) && BooleanUtils.toBoolean(value);
    }

    private String decodeHeaderValue(HttpServletRequest request, String value) {
        try {
            // 如果value被encode过，才进行decode
            String swithEncodeDecode = EnvUtil.getProperty("supercode.header.encodedecode.enabled", "false");
            if (isHeaderEncoded(request) && BooleanUtils.toBoolean(swithEncodeDecode)) {
                return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
            }
        } catch (Exception ignore) {
        }
        return value;
    }
}
