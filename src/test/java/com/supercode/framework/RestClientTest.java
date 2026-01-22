package com.supercode.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.supercode.framework.net.RestClient;
import com.supercode.master.utils.json.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

@Disabled
@Slf4j
public class RestClientTest {

    private final String userName = "5738011f-9ed0-4f6c-a2dc-14d56d623e2c";
    private final String password = "test-3dq8yqVn1RyUL7zjF3CfFXzje1GY2rKaEo8dmjzswuFAfhAbgUGhsdQ3A1v2et3m";

    @Test
    public void testGet() {
        String responseStr = RestClient.DEFAULT.getAuth("https://app.moderntreasury.com/api/counterparties", userName, password, String.class, null, null);
        log.info("RestClientTest.responseStr is {}", responseStr);
        Assertions.assertNotNull(responseStr);
    }

    @Test
    public void testGetForOriResponse() {
        HttpResponse httpResponse = RestClient.DEFAULT.getAuth("https://app.moderntreasury.com/api/counterparties", userName, password, HttpResponse.class, null, null);
        log.info("RestClientTest.responseStr is {}", JacksonUtil.toJsonStr(httpResponse));
        Assertions.assertNotNull(httpResponse);
    }

    /**
     * RestClient.DEFAULT.get("https://app.moderntreasury.com/api/counterparties", new TypeReference<List<TestDemo>>(){}, paramsMap, headMap);
     */
    @Test
    public void testGetWithParams() {
        Map<String, String> headMap = new HashMap<>();
        headMap.put("Authorization", "Basic NTczODAxMWYtOWVkMC00ZjZjLWEyZGMtMTRkNTZkNjIzZTJjOnRlc3QtM2RxOHlxVm4xUnlVTDd6akYzQ2ZGWHpqZTFHWTJyS2FFbzhkbWp6c3d1RkFmaEFiZ1VHaHNkUTNBMXYyZXQzbQ==");

        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("page", 1);
        paramsMap.put("per_page", 1);

        String responseStr = RestClient.DEFAULT.get("https://app.moderntreasury.com/api/counterparties", String.class, paramsMap, headMap);
        log.info("RestClientTest.responseStr is {}", responseStr);
        Assertions.assertNotNull(responseStr);
    }

    @Test
    public void testPost() {
        String url = "https://app.moderntreasury.com/api/counterparties";
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonObj = mapper.createObjectNode();
        jsonObj.put("name", "Jonathan Ji");
        jsonObj.put("email", "jonathan.ji@example.com");
        String result = RestClient.DEFAULT.postJsonAuth(url, userName, password, jsonObj, String.class, null, null);
        Assertions.assertNotNull(result);
    }

    @Test
    public void testDelete() {
        String url = "https://app.moderntreasury.com/api/counterparties";
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonObj = mapper.createObjectNode();
        jsonObj.put("name", "Jonathan Ji");
        jsonObj.put("email", "jonathan.ji@example.com");
        String result = RestClient.DEFAULT.postJsonAuth(url, userName, password, jsonObj, String.class, null, null);
        Assertions.assertNotNull(result);
    }

    @Test
    public void testPatch() {
        String url = "https://app.moderntreasury.com/api/counterparties/958ec1dd-e177-4219-80de-8f2d9ab875b7";
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode jsonObj = mapper.createObjectNode();
        jsonObj.put("account_type", "checking");

        ArrayNode arrayNode = mapper.createArrayNode();
        arrayNode.add(jsonObj);

        ObjectNode jsonObj2 = mapper.createObjectNode();
        jsonObj2.put("accounts", arrayNode);

        String result = RestClient.DEFAULT.patch(url, userName, password, jsonObj2, String.class, null, null);
        Assertions.assertNotNull(result);
    }
}
