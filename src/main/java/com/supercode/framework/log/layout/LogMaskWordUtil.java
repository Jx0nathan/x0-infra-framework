package com.supercode.framework.log.layout;

//import com.alibaba.fastjson2.JSON;
//import com.alibaba.fastjson2.JSONArray;
//import com.alibaba.fastjson2.JSONObject;
//import com.alibaba.fastjson2.filter.ValueFilter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.supercode.framework.ctypto.utils.Md5Tools;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//import static com.alibaba.fastjson2.JSONWriter.Feature.UseSingleQuotes;

/**
 * @author jonathan.ji
 */
public class LogMaskWordUtil {
    private static final Logger logger = LoggerFactory.getLogger(LogMaskWordUtil.class);

    private static final String[] DEFAULT_MASK_KEYS = {"salt", "password", "passwordConfirm", "secretKey",
            "registerToken", "newPassword", "oldPassword", "token", "antiPhishingCode", "serialNo", "disableToken",
            "csrfToken", "serialNoValue", "secretKey", "cookie", "code", "forbiddenLink", "link", "recipient", "mobile", "authKey", "device_info",
            "deviceInfo", "mobileCode", "mobileNum", "mobile_code", "tableAntiPhishing", "antiCode", "verifyCode", "verify_code", "emailVerifyCode",
            "smsCode", "googleCode", "apiKey", "appKey", "authorization", "signature", "apiSecret", "contrastImage", "sourceImage", "apiSecret",
            "accessToken", "refreshToken", "parentCode", "apikey", "taxId", "deviceInfo", "cardNumber", "cardNum", "cvv", "lastName", "firstName", "expiryMonth",
            "expiryYear", "safePassword", "confirmSafePassword", "mobileVerifyCode", "googleVerifyCode", "emailVerifyCode", "yubikeyVerifyCode", "payAccount", "phone",
            "pan", "customerRequestBody", "kycRequestBody"};

    private static final Set<String> STRING_HASH_SET = new HashSet<>(Arrays.asList(DEFAULT_MASK_KEYS));

    /**
     * customized mask keys and whether to hash the value.
     */
    private static final Map<String, Boolean> HASH_MAP = new ConcurrentHashMap<>();
    //    static ValueFilter defaultMaskFilter = new MaskValueFilter();
    // serialize with jackson
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);

        SensitiveWordInfoModule module = new SensitiveWordInfoModule();
        module.addSerializer(String.class, new AutoWordMaskSerializer(String.class));
        module.addSerializer(Integer.class, new AutoWordMaskSerializer(Integer.class));
        module.addSerializer(Long.class, new AutoWordMaskSerializer(Long.class));
        objectMapper.registerModule(module);
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * add additional mask keys that defined by users.
     */
    public static void init(List<String> additionalMaskKeys) {
        if (additionalMaskKeys != null) {
            for (String key : additionalMaskKeys) {
                initWithKey(key);
            }
        }
    }

    private static void initWithKey(String key) {
        if (key.length() > 1 && key.endsWith("#")) {
            key = key.substring(0, key.length() - 1);
            HASH_MAP.put(key, Boolean.TRUE);
        } else {
            HASH_MAP.put(key, Boolean.FALSE);
        }
    }

    public static String fastMaskJsonStr(String jsonStr) {
        if (StringUtils.isNotBlank(jsonStr)) {
            try {
                JsonNode node = objectMapper.readTree(jsonStr);
                if (node == null) {
                    logger.warn("cannot parse from json string");
                    return jsonStr;
                }
                doMaskJsonNode(node);
                return objectMapper.writeValueAsString(node);
            } catch (IOException e) {
                logger.error("fail to mask json object", e);
            }
        }
        return jsonStr;
    }

    private static String getMaskedStr(String value, String name) {
        Boolean hashMaskFlag = HASH_MAP.get(name);
        String result = null;
        if (Boolean.TRUE.equals(hashMaskFlag) && value != null) {
            result = "##>" + Md5Tools.MD5(value);
        } else if (hashMaskFlag != null) {
            result = "***";
        }
        if (STRING_HASH_SET.contains(name)) {
            result = "******";
        }
        return result;
    }

    private static void doMaskJsonNode(JsonNode node) {
        Iterator<Map.Entry<String, JsonNode>> itr;
        for (itr = node.fields(); itr.hasNext(); ) {
            Map.Entry<String, JsonNode> n = itr.next();
            if (HASH_MAP.containsKey(n.getKey()) || STRING_HASH_SET.contains(n.getKey())) {
                if (n.getValue().getNodeType() != JsonNodeType.NULL) {
                    String value = n.getValue().asText();
                    n.setValue(new TextNode(getMaskedStr(value, n.getKey())));
                }
            } else {
                doMaskJsonNode(n.getValue());
            }
        }
    }

//    private static String getMaskJson(JSON jsonObject) {
//        if (jsonObject == null) {
//            return "";
//        }
//        return getMaskJson(jsonObject, defaultMaskFilter);
//    }
//
//    private static String getMaskJson(JSON jsonObject, ValueFilter valueFilter) {
//        if (jsonObject instanceof JSONObject) {
//            String result = toJSONString(valueFilter, (JSONObject) jsonObject);
//            return result;
//        } else if (jsonObject instanceof JSONArray) {
//            JSONArray jsonArray = (JSONArray) jsonObject;
//            if (jsonObject == null && jsonArray == null) {
//                return org.apache.commons.lang3.StringUtils.EMPTY;
//            }
//            StringBuilder buf = new StringBuilder();
//            if (null != jsonArray && jsonArray.size() > 0) {
//                jsonArray.stream().forEach(e -> {
//                    if (e instanceof JSON) {
//                        buf.append(getMaskJson((JSON) e, valueFilter));
//                    } else if (e != null) {
//                        buf.append("\"" + e.toString() + "\",");
//                    }
//                });
//            }
//            if (buf.length() > 0 && buf.charAt(buf.length() - 1) == ',') {
//                buf.insert(0, '[');
//                buf.setCharAt(buf.length() - 1, ']');
//            }
//            return buf.toString();
//        }
//        return "";
//    }
//
//    private static String toJSONString(ValueFilter filter, JSONObject jsonObject) {
//        return JSON.toJSONString(jsonObject, filter, UseSingleQuotes);
//    }
//
//    /**
//     * filter to process the json object filed value to mask strings.
//     */
//    static class MaskValueFilter implements ValueFilter {
//
//        public MaskValueFilter(String... maskKeys) {
//            if (maskKeys.length > 0) {
//                for (String key : maskKeys) {
//                    initWithKey(key);
//                }
//            }
//        }
//
//        @Override
//        public Object apply(Object o, String name, Object value) {
//            Boolean hashMaskFlag = HASH_MAP.get(name);
//            if (Boolean.TRUE.equals(hashMaskFlag)) {
//                return value == null ? "null" : "##>" + Md5Tools.MD5(value.toString());
//            } else if (hashMaskFlag != null) {
//                return "***";
//            }
//            if (STRING_HASH_SET.contains(name)) {
//                return "******";
//            }
//            return value;
//        }
//    }

    static class SensitiveWordInfoModule extends SimpleModule {
        private static final long serialVersionUID = 1L;

        @Override
        public Object getTypeId() {
            return SensitiveWordInfoModule.class;
        }
    }

    static class AutoWordMaskSerializer<T> extends StdSerializer<T> {
        private JsonSerializer base;

        protected AutoWordMaskSerializer(Class<T> t) {
            super(t);
            if (Integer.class.isAssignableFrom(t)) {
                base = new NumberSerializers.IntegerSerializer(t);
            } else if (Long.class.isAssignableFrom(t)) {
                base = new NumberSerializers.LongSerializer(t);
            }
        }

        @Override
        public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            String name = gen.getOutputContext().getCurrentName();
            if (name == null) {
                return;
            }
            String result = getMaskedStr(value != null ? value.toString() : null, name);
            if (result != null) {
                gen.writeString(result);
            } else if (base != null) {
                base.serialize(value, gen, serializers);
            } else {
                gen.writeString(value != null ? value.toString() : "null");
            }
        }
    }
}
