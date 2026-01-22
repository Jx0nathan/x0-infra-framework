package com.supercode.framework.log.layout;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.supercode.master.constant.HeaderConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.util.ReadOnlyStringMap;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @Author: jonathan.ji
 * @Date: 2022/1/23 21:37
 * @Desc:
 */
@Plugin(name = "CustomJsonLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE, printObject = true)
public class CustomJsonLayout extends AbstractStringLayout {

    public static final String MIXEDFIELDS_APP_NAME = "appName";
    public static final String REQUEST_TIME = "requestTime";
    public static final String TRACE_ID = "traceId";
    public static final String PIN_POINT_ID = "PtxId";
    public static final String X_AMZN_TRACE_ID = "x-amzn-trace-id";
    public static final String MIXEDFIELDS_HOST_NAME = "ip";
    private static final String DEFAULT_FOOTER = "]";
    private static final String DEFAULT_HEADER = "[";
    private final ObjectMapper objectMapper;

    CustomJsonLayout(final Configuration config, final String headerPattern, final String footerPattern,
                     final Charset charset, final Map<String, String> mixedFields) {
        super(config, charset,
                PatternLayout.createSerializer(config, null, headerPattern, DEFAULT_HEADER, null, false, false),
                PatternLayout.createSerializer(config, null, footerPattern, DEFAULT_FOOTER, null, false, false));
        SimpleModule module = new SimpleModule();
        module.addSerializer(LogEvent.class, new LogEventSerializer(mixedFields));
        module.addSerializer(Throwable.class, new ThrowableSerializer());
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    @PluginFactory
    public static CustomJsonLayout createLayout(@PluginConfiguration final Configuration config,
                                                @PluginAttribute(value = "header", defaultString = DEFAULT_HEADER) final String headerPattern,
                                                @PluginAttribute(value = "footer", defaultString = DEFAULT_FOOTER) final String footerPattern,
                                                @PluginAttribute(value = "charset", defaultString = "UTF-8") final Charset charset,
                                                @PluginAttribute(value = "mixedFields") final Map<String, String> mixedFields) {
        return new CustomJsonLayout(config, headerPattern, footerPattern, charset, mixedFields);
    }

    public static CustomJsonLayout createDefaultLayout(Configuration config, Map<String, String> mixedFields) {
        return new CustomJsonLayout(config, DEFAULT_HEADER, DEFAULT_FOOTER, StandardCharsets.UTF_8, mixedFields);
    }

    @Override
    public String toSerializable(LogEvent logEvent) {
        try {
            String text = objectMapper.writeValueAsString(logEvent);
            return text;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class LogEventSerializer extends StdSerializer<LogEvent> {

        private static final long serialVersionUID = 1L;
        private final Map<String, String> mixedFields;

        LogEventSerializer(Map<String, String> mixedFields) {
            super(LogEvent.class);
            this.mixedFields = mixedFields;
        }

        private static String traceId(ReadOnlyStringMap mdc) {
            return StringUtils.defaultIfBlank(mdc.getValue(TRACE_ID), StringUtils.EMPTY);
        }

        private static String requestTime(ReadOnlyStringMap mdc) {
            return StringUtils.defaultIfBlank(mdc.getValue(REQUEST_TIME), "0");
        }

        private static String userId(ReadOnlyStringMap mdc) {
            return StringUtils.defaultIfBlank(mdc.getValue(HeaderConstant.HEADER_USER_ID), StringUtils.EMPTY);
        }

        private static String pinPointId(ReadOnlyStringMap mdc) {
            String pinpointId = mdc.getValue(PIN_POINT_ID);
            if (StringUtils.isBlank(pinpointId)) {
                pinpointId = StringUtils.EMPTY;
            }
            return pinpointId;
        }

        private static String awsId(ReadOnlyStringMap mdc) {
            String awsId = mdc.getValue(X_AMZN_TRACE_ID);
            if (StringUtils.isBlank(awsId)) {
                awsId = StringUtils.EMPTY;
            }
            return awsId;
        }

        @Override
        public void serialize(LogEvent value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeStringField(MIXEDFIELDS_APP_NAME, mixedFields.get(MIXEDFIELDS_APP_NAME));
            gen.writeStringField(MIXEDFIELDS_HOST_NAME, mixedFields.get(MIXEDFIELDS_HOST_NAME));
            ReadOnlyStringMap mdc = value.getContextData();
            gen.writeStringField("level", value.getLevel().name());
            gen.writeStringField("uuid", traceId(mdc));
            gen.writeStringField("thread", value.getThreadName());
            gen.writeStringField("costTime", requestTime(mdc));
            gen.writeStringField("userId", userId(mdc));
            gen.writeStringField("pinpointId", pinPointId(mdc));
            gen.writeStringField("awsId", awsId(mdc));
            gen.writeNumberField("timestamp", value.getTimeMillis());
            gen.writeStringField("class", value.getLoggerName());
            gen.writeStringField("message", buildMessage(value));
            gen.writeEndObject();
        }

        private String buildMessage(LogEvent value) {
            StringBuilder message = new StringBuilder();
            message.append(value.getMessage().getFormattedMessage());
            Throwable throwable = value.getThrown();
            if (throwable != null) {
                message.append(" ");
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                throwable.printStackTrace(printWriter);
                StringBuffer error = stringWriter.getBuffer();
                message.append(error.toString());
            }
            return message.toString();
        }
    }


    private static class ThrowableSerializer extends StdSerializer<Throwable> {

        private static final long serialVersionUID = 1L;

        ThrowableSerializer() {
            super(Throwable.class);
        }

        @Override
        public void serialize(Throwable value, JsonGenerator gen, SerializerProvider provider) {
            try (StringWriter stringWriter = new StringWriter()) {
                try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
                    value.printStackTrace(printWriter);
                    gen.writeString(stringWriter.toString());
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

}
