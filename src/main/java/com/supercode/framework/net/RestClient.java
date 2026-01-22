package com.supercode.framework.net;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.supercode.framework.constant.HttpHeader;
import com.supercode.framework.exception.RestClientException;
import com.supercode.master.utils.json.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings({"unused", "unchecked"})
@Slf4j
public final class RestClient {

    public static final RestClient LOW = new RestClient(300, 300, 15, 50);
    public static final RestClient DEFAULT = new RestClient(1000, 1000, 15, 50);
    public static final RestClient HIGH = new RestClient(2000, 2000, 15, 50);
    public static final RestClient DANGER_HIGH = new RestClient(18000, 18000);

    private final RequestConfig requestConfig;
    private final HttpClient httpClient;

    /**
     * Determines the timeout in milliseconds until a connection is established. A timeout value of zero is interpreted as an infinite timeout
     * Please note this parameter can only be applied to connections that are bound to a particular local address.
     */
    private final int connectTimeout;

    /**
     * timeout for waiting for data or, put differently, a maximum period inactivity between two consecutive data packets
     * A timeout value of zero is interpreted as an infinite timeout.
     */
    private final int socketTimeout;

    /**
     * a maximum limit of connection on a per route
     */
    private final int maxConnPerRoute;

    /**
     * a maximum limit of connection on a pool
     */
    private final int maxConnPerTotal;
    private final ResponseHandler<String> responseHandler = response -> {
        int status = response.getStatusLine().getStatusCode();
        HttpEntity entity;
        if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
            entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
        } else {
            String errorMsg = null;
            if ((entity = response.getEntity()) != null) {
                errorMsg = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            }
            throw new HttpResponseException(status, errorMsg);
        }
    };
    /**
     * user customize ssl connection factory
     */
    private SSLConnectionSocketFactory connectionSocketFactory;

    public RestClient(int connectTimeout, int socketTimeout) {
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
        this.maxConnPerRoute = 2;
        this.maxConnPerTotal = 20;
        this.requestConfig = buildRequestConfig();
        this.httpClient = newHttpClient();
    }

    public RestClient(int connectTimeout, int socketTimeout, SSLConnectionSocketFactory connectionSocketFactory) {
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
        this.maxConnPerRoute = 2;
        this.maxConnPerTotal = 20;
        this.connectionSocketFactory = connectionSocketFactory;
        this.requestConfig = buildRequestConfig();
        this.httpClient = newHttpClient();
    }

    public RestClient(int connectTimeout, int socketTimeout, int maxConnPerRoute, int maxConnPerTotal) {
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
        this.maxConnPerRoute = maxConnPerRoute;
        this.maxConnPerTotal = maxConnPerTotal;
        this.requestConfig = buildRequestConfig();
        this.httpClient = newHttpClient();
    }


    public RestClient(int connectTimeout, int socketTimeout, int maxConnPerRoute, int maxConnPerTotal, SSLConnectionSocketFactory connectionSocketFactory) {
        this.connectTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
        this.maxConnPerRoute = maxConnPerRoute;
        this.maxConnPerTotal = maxConnPerTotal;
        this.connectionSocketFactory = connectionSocketFactory;
        this.requestConfig = buildRequestConfig();
        this.httpClient = newHttpClient();
    }

    /**
     * @param requestConfig
     * @param maxConnPerRoute
     * @param maxConnPerTotal
     * @param connectionSocketFactory will use SSLConnectionSocketFactory.getSocketFactory() if null
     */
    public RestClient(RequestConfig requestConfig, int maxConnPerRoute, int maxConnPerTotal, SSLConnectionSocketFactory connectionSocketFactory) {
        // 不会使用这两个参数
        this.connectTimeout = -1;
        this.socketTimeout = -1;

        this.maxConnPerRoute = maxConnPerRoute;
        this.maxConnPerTotal = maxConnPerTotal;
        this.requestConfig = requestConfig;
        this.connectionSocketFactory = connectionSocketFactory;
        this.httpClient = newHttpClient();
    }

    private HttpClient newHttpClient() {
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", Objects.nonNull(this.connectionSocketFactory) ? this.connectionSocketFactory : SSLConnectionSocketFactory.getSocketFactory()).build();

        //设置连接池大小
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        connManager.setMaxTotal(this.maxConnPerTotal);
        connManager.setDefaultMaxPerRoute(this.maxConnPerRoute);

        return HttpClients.custom().setKeepAliveStrategy(new DefaultDurationConnectionKeepAliveStrategy())
                .setConnectionManager(connManager).build();
    }

    protected RequestConfig buildRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(this.connectTimeout)
                .setSocketTimeout(this.socketTimeout)
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();
    }

    /**
     * 发送GET请求-无请求头，无尾部参数
     */
    public <T> T get(String uri, Class<T> clazz) throws RestClientException {
        return get(uri, clazz, null);
    }

    /**
     * 发送GET请求-无请求头
     */
    public <T> T get(String uri, Class<T> clazz, Map<String, ?> params) throws RestClientException {
        return get(uri, clazz, params, null);
    }

    /**
     * 用户认证,发送GET请求
     */
    public <T> T getAuth(String uri, String userName, String password, Class<T> clazz, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        headers = this.setBasicAuth(userName, password, headers);
        return get(uri, clazz, params, headers);
    }

    /**
     * 发送GET请求-无请求头，无尾部参数
     */
    public <T> T get(String uri, TypeReference<T> reference) throws RestClientException {
        return get(uri, reference, null);
    }

    /**
     * 发送GET请求-无请求头
     */
    public <T> T get(String uri, TypeReference<T> reference, Map<String, ?> params) throws RestClientException {
        return get(uri, reference, params, null);
    }

    public <T> T getAuth(String uri, String userName, String password, TypeReference<T> reference, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        headers = this.setBasicAuth(userName, password, headers);
        return get(uri, reference, params, headers);
    }

    public <T> T postJson(String uri, Object request, Class<T> clazz) throws RestClientException {
        return postJson(uri, request, clazz, null);
    }

    public <T> T postJson(String uri, Object request, Class<T> clazz, Map<String, ?> params) throws RestClientException {
        return postJson(uri, request, clazz, params, null);
    }

    public <T> T postJson(String uri, Object request, TypeReference<T> reference, Map<String, ?> params) throws RestClientException {
        return postJson(uri, request, reference, params, null);
    }

    public <T> T postJson(String uri, Object request, TypeReference<T> reference, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        return post(uri, request, reference, params, headers, true);
    }

    public <T> T postJson(String uri, Object request, Class<T> clazz, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        return post(uri, request, clazz, params, headers, true);
    }

    public <T> T postJsonAuth(String uri, String userName, String password, Object request, TypeReference<T> reference, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        headers = this.setBasicAuth(userName, password, headers);
        return post(uri, request, reference, params, headers, true);
    }

    public <T> T postJsonAuth(String uri, String userName, String password, Object request, Class<T> clazz, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        headers = this.setBasicAuth(userName, password, headers);
        return post(uri, request, clazz, params, headers, true);
    }

    public <T> T postAuth(String uri, String userName, String password, Object request, Class<T> clazz, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        headers = this.setBasicAuth(userName, password, headers);
        return post(uri, request, clazz, params, headers, false);
    }

    public <T> T putJson(String uri, Object request, Class<T> clazz) throws RestClientException {
        return putJson(uri, request, clazz, null);
    }

    public <T> T putJson(String uri, Object request, Class<T> clazz, Map<String, ?> params) throws RestClientException {
        return putJson(uri, request, clazz, params, null);
    }

    public <T> T putJson(String uri, Object request, TypeReference<T> reference, Map<String, ?> params) throws RestClientException {
        return putJson(uri, request, reference, params, null);
    }

    public <T> T putJson(String uri, Object request, TypeReference<T> reference, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        return put(uri, request, reference, params, headers, true);
    }

    public <T> T putJson(String uri, Object request, Class<T> clazz, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        return put(uri, request, clazz, params, headers, true);
    }

    public <T> T putJsonAuth(String uri, String userName, String password, Object request, TypeReference<T> reference, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        headers = this.setBasicAuth(userName, password, headers);
        return put(uri, request, reference, params, headers, true);
    }

    public <T> T putJsonAuth(String uri, String userName, String password, Object request, Class<T> clazz, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        headers = this.setBasicAuth(userName, password, headers);
        return put(uri, request, clazz, params, headers, true);
    }

    public <T> T putAuth(String uri, String userName, String password, Object request, Class<T> clazz, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        headers = this.setBasicAuth(userName, password, headers);
        return put(uri, request, clazz, params, headers, false);
    }

    public <T> T patch(String uri, String userName, String password, Object request, Class<T> clazz, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        headers = this.setBasicAuth(userName, password, headers);
        return patch(uri, request, clazz, params, headers, true);
    }

    public <T> T postForm(String uri, Object request, Class<T> clazz) throws RestClientException {
        return postForm(uri, request, clazz, null);
    }

    public <T> T postForm(String uri, Object request, Class<T> clazz, Map<String, ?> params) throws RestClientException {
        return postForm(uri, request, clazz, params, null);
    }

    public <T> T postForm(String uri, Object request, Class<T> clazz, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        return post(uri, request, clazz, params, headers, false);
    }

    public <T> T postForm(String uri, Object request, TypeReference<T> reference, Map<String, ?> params) throws RestClientException {
        return postForm(uri, request, reference, params, null);
    }

    public <T> T postForm(String uri, Object request, TypeReference<T> reference, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        return post(uri, request, reference, params, headers, false);
    }

    public <T> T delete(String uri, Class<T> clazz) throws RestClientException {
        return delete(uri, clazz, null);
    }

    public <T> T delete(String uri, Class<T> clazz, Map<String, ?> params) throws RestClientException {
        return delete(uri, clazz, params, null);
    }

    public <T> T delete(String uri, String userName, String password, Class<T> clazz, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        headers = this.setBasicAuth(userName, password, headers);
        return delete(uri, clazz, params, headers);
    }

    /**
     * 发送GET请求
     *
     * @param uri     URI
     * @param clazz   期望得到的数据返回类型
     * @param params  拼接在URI尾部的参数
     * @param headers 请求头
     */
    public <T> T get(String uri, Class<T> clazz, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        String aUri = buildUrl(uri, params);
        log.debug("Start GET uri[{}] with params[{}] header[{}]", uri, params, headers);
        String body = null;
        try {
            HttpGet httpGet = buildHttpGet(aUri, headers);
            if (clazz == HttpResponse.class) {
                return (T) httpClient.execute(httpGet);
            }
            body = executeRequest(httpGet);
            log.debug("Done GET uri[{}] with params[{}] header[{}], and response is {}", uri, params, headers, body);
            if (clazz == String.class) {
                return (T) body;
            }
            if (body != null) {
                return JacksonUtil.from(body, clazz);
            }
        } catch (Exception e) {
            log.warn("Occur error when GET uri[{}] with params[{}] headers[{}], response is {}, error: {}", uri, params, headers, body, e);
            throw new RestClientException(e.getMessage(), e);
        }
        return null;
    }

    public <T> T get(String uri, TypeReference<T> reference, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        String aUri = buildUrl(uri, params);
        log.debug("Start GET uri[{}] with params[{}] header[{}]", uri, params, headers);
        String body = null;
        try {
            HttpGet httpGet = buildHttpGet(aUri, headers);
            body = executeRequest(httpGet);
            log.debug("Done GET uri[{}] with params[{}] header[{}], and response is {}", uri, params, headers, body);
            if (body != null) {
                return JacksonUtil.from(body, reference);
            }
        } catch (Exception e) {
            log.warn("Occur error when GET uri[{}] with params[{}] headers[{}], response is {}, error: {}", uri, params, headers, body, e);
            throw new RestClientException(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 发送POST请求
     *
     * @param uri      URI
     * @param clazz    期望得到的数据返回类型
     * @param params   拼接在URI尾部的参数
     * @param headers  请求头
     * @param jsonPost 是否json请求体
     */
    public <T> T post(String uri, Object request, Class<T> clazz, Map<String, ?> params, Map<String, String> headers, boolean jsonPost) throws RestClientException {
        String aUri = buildUrl(uri, params);
        log.debug("Start POST uri[{}] with params[{}] header[{}] request[{}]", uri, params, headers, request);
        String body = null;
        try {
            HttpPost httpPost = buildHttpPost(aUri, headers, request, jsonPost);
            if (clazz == HttpResponse.class) {
                return (T) httpClient.execute(httpPost);
            }
            body = executeRequest(httpPost);
            log.debug("Done POST uri[{}] with params[{}] header[{}] request[{}], and response is {}", uri, params, headers, request, body);
            if (clazz == String.class) {
                return (T) body;
            }
            if (body != null) {
                return JacksonUtil.from(body, clazz);
            }
        } catch (Exception e) {
            log.warn("Occur error when POST uri[{}] with params[{}] headers[{}] request[{}], response is {}, error: {}", uri, params, headers, request, body, e);
            throw new RestClientException(e.getMessage(), e);
        }
        return null;
    }

    public <T> T post(String uri, Object request, TypeReference<T> reference, Map<String, ?> params, Map<String, String> headers, boolean jsonPost) throws RestClientException {
        String aUri = buildUrl(uri, params);
        log.debug("Start POST uri[{}] with params[{}] header[{}] request[{}]", uri, params, headers, request);
        String body = null;
        try {
            HttpPost httpPost = buildHttpPost(aUri, headers, request, jsonPost);
            body = executeRequest(httpPost);
            log.debug("Done POST uri[{}] with params[{}] header[{}] request[{}], and response is {}", uri, params, headers, request, body);
            if (body != null) {
                return JacksonUtil.from(body, reference);
            }
        } catch (Exception e) {
            log.warn("Occur error when POST uri[{}] with params[{}] headers[{}] request[{}], response is {}, error: {}", uri, params, headers, request, body, e);
            throw new RestClientException(e.getMessage(), e);
        }
        return null;
    }

    public <T> T put(String uri, Object request, Class<T> clazz, Map<String, ?> params, Map<String, String> headers, boolean jsonPut) throws RestClientException {
        String aUri = buildUrl(uri, params);
        log.debug("Start PUT uri[{}] with params[{}] header[{}] request[{}]", uri, params, headers, request);
        String body = null;
        try {
            HttpPut httpPut = buildHttpPut(aUri, headers, request, jsonPut);
            if (clazz == HttpResponse.class) {
                return (T) httpClient.execute(httpPut);
            }
            body = executeRequest(httpPut);
            log.debug("Done PUT uri[{}] with params[{}] header[{}] request[{}], and response is {}", uri, params, headers, request, body);
            if (clazz == String.class) {
                return (T) body;
            }
            if (body != null) {
                return JacksonUtil.from(body, clazz);
            }
        } catch (Exception e) {
            log.warn("Occur error when PUT uri[{}] with params[{}] headers[{}] request[{}], response is {}, error: {}", uri, params, headers, request, body, e);
            throw new RestClientException(e.getMessage(), e);
        }
        return null;
    }

    public <T> T put(String uri, Object request, TypeReference<T> reference, Map<String, ?> params, Map<String, String> headers, boolean jsonPut) throws RestClientException {
        String aUri = buildUrl(uri, params);
        log.debug("Start PUT uri[{}] with params[{}] header[{}] request[{}]", uri, params, headers, request);
        String body = null;
        try {
            HttpPut httpPut = buildHttpPut(aUri, headers, request, jsonPut);
            body = executeRequest(httpPut);
            log.debug("Done PUT uri[{}] with params[{}] header[{}] request[{}], and response is {}", uri, params, headers, request, body);
            if (body != null) {
                return JacksonUtil.from(body, reference);
            }
        } catch (Exception e) {
            log.warn("Occur error when PUT uri[{}] with params[{}] headers[{}] request[{}], response is {}, error: {}", uri, params, headers, request, body, e);
            throw new RestClientException(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 发送PATCH请求
     *
     * @param uri     URI
     * @param clazz   期望得到的数据返回类型
     * @param params  拼接在URI尾部的参数
     * @param headers 请求头
     */
    public <T> T patch(String uri, Object request, Class<T> clazz, Map<String, ?> params, Map<String, String> headers, boolean jsonPatch) throws RestClientException {
        String aUri = buildUrl(uri, params);
        log.debug("Start PATCH uri[{}] with params[{}] header[{}]", uri, params, headers);
        String body = null;
        try {
            HttpPatch httpPatch = buildHttpPatch(aUri, headers, request, jsonPatch);
            if (clazz == HttpResponse.class) {
                return (T) httpClient.execute(httpPatch);
            }
            body = httpClient.execute(httpPatch, this.responseHandler);
            log.debug("Done PATCH uri[{}] with params[{}] header[{}], and response is {}", uri, params, headers, body);
            if (clazz == String.class) {
                return (T) body;
            }
            if (body != null) {
                return JacksonUtil.from(body, clazz);
            }
        } catch (Exception e) {
            log.warn("Occur error when PATCH uri[{}] with params[{}] headers[{}], response is {}, error: {}", uri, params, headers, body, e);
            throw new RestClientException(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 发送DELETE请求
     *
     * @param uri     URI
     * @param clazz   期望得到的数据返回类型
     * @param params  拼接在URI尾部的参数
     * @param headers 请求头
     */
    public <T> T delete(String uri, Class<T> clazz, Map<String, ?> params, Map<String, String> headers) throws RestClientException {
        String aUri = buildUrl(uri, params);
        log.debug("Start DELETE uri[{}] with params[{}] header[{}]", uri, params, headers);
        String body = null;
        try {
            HttpDelete httpDel = buildHttpDelete(aUri, headers);
            if (clazz == HttpResponse.class) {
                return (T) httpClient.execute(httpDel);
            }
            body = httpClient.execute(httpDel, this.responseHandler);
            log.debug("Done DELETE uri[{}] with params[{}] header[{}], and response is {}", uri, params, headers, body);
            if (clazz == String.class) {
                return (T) body;
            }
            if (body != null) {
                return JacksonUtil.from(body, clazz);
            }
        } catch (Exception e) {
            log.warn("Occur error when DELETE uri[{}] with params[{}] headers[{}], response is {}, error: {}", uri, params, headers, body, e);
            throw new RestClientException(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 参数拼接URL
     */
    private String buildUrl(String uri, Map<String, ?> params) throws RestClientException {
        String aUri = uri;
        List<NameValuePair> paramPairList = buildParams(params);
        if (null != paramPairList) {
            aUri += "?" + URLEncodedUtils.format(paramPairList, "utf-8");
        }
        return aUri;
    }

    private List<NameValuePair> buildParams(Map<String, ?> params) {
        if (params == null) {
            return null;
        }
        List<NameValuePair> resultList = new ArrayList<>();
        for (Map.Entry<String, ?> param : params.entrySet()) {
            resultList.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
        }
        return resultList;
    }

    /**
     * 设置请求头
     */
    private void setHeaders(HttpUriRequest httpUriRequest, Map<String, String> headers) {
        if (null == headers) {
            return;
        }
        for (Map.Entry<String, String> header : headers.entrySet()) {
            httpUriRequest.addHeader(header.getKey(), header.getValue());
        }
    }

    /**
     * 构建HttpGet
     */
    private HttpGet buildHttpGet(String uri, Map<String, String> headers) {
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setConfig(this.requestConfig);
        setHeaders(httpGet, headers);
        return httpGet;
    }

    /**
     * 构建HttpPost
     */
    private HttpPost buildHttpPost(String uri, Map<String, String> headers, Object request, boolean jsonPost) {
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setConfig(this.requestConfig);
        setHeaders(httpPost, headers);
        if (null != request) {
            if (jsonPost) {
                httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                StringEntity entity = new StringEntity((request instanceof String ? request.toString() : JacksonUtil.toJsonStr(request)), ContentType.APPLICATION_JSON);
                httpPost.setEntity(entity);
            } else {
                Preconditions.checkArgument(request instanceof Map, "request must be Map<String, ?>");
                StringEntity entity = new UrlEncodedFormEntity(buildParams((Map<String, ?>) request), StandardCharsets.UTF_8);
                httpPost.setEntity(entity);
            }
        }
        return httpPost;
    }

    /**
     * 构建HttpPut
     */
    private HttpPut buildHttpPut(String uri, Map<String, String> headers, Object request, boolean jsonPut) {
        HttpPut httpPut = new HttpPut(uri);
        httpPut.setConfig(this.requestConfig);
        setHeaders(httpPut, headers);
        if (null != request) {
            if (jsonPut) {
                httpPut.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                StringEntity entity = new StringEntity((request instanceof String ? request.toString() : JacksonUtil.toJsonStr(request)), ContentType.APPLICATION_JSON);
                httpPut.setEntity(entity);
            } else {
                Preconditions.checkArgument(request instanceof Map, "request must be Map<String, ?>");
                StringEntity entity = new UrlEncodedFormEntity(buildParams((Map<String, ?>) request), StandardCharsets.UTF_8);
                httpPut.setEntity(entity);
            }
        }
        return httpPut;
    }

    /**
     * 构建HttpPatch
     */
    private HttpPatch buildHttpPatch(String uri, Map<String, String> headers, Object request, boolean jsonPatch) {
        HttpPatch httpPatch = new HttpPatch(uri);
        httpPatch.setConfig(this.requestConfig);
        setHeaders(httpPatch, headers);
        if (null != request) {
            if (jsonPatch) {
                httpPatch.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                StringEntity entity = new StringEntity((request instanceof String ? request.toString() : JacksonUtil.toJsonStr(request)), ContentType.APPLICATION_JSON);
                httpPatch.setEntity(entity);
            } else {
                Preconditions.checkArgument(request instanceof Map, "request must be Map<String, ?>");
                StringEntity entity = new UrlEncodedFormEntity(buildParams((Map<String, ?>) request), StandardCharsets.UTF_8);
                httpPatch.setEntity(entity);
            }
        }
        return httpPatch;
    }

    /**
     * 构建HttpDelete
     */
    private HttpDelete buildHttpDelete(String uri, Map<String, String> headers) {
        HttpDelete httpDelete = new HttpDelete(uri);
        httpDelete.setConfig(this.requestConfig);
        setHeaders(httpDelete, headers);
        return httpDelete;
    }

    private Map<String, String> setBasicAuth(String userName, String password, Map<String, String> headers) {
        if (headers == null) {
            headers = new HashMap<>(2);
        }
        String basicAuthInfo = this.buildBasicAuth(userName == null ? "" : userName, password == null ? "" : password);
        headers.put(HttpHeader.AUTHORIZATION.getValue(), basicAuthInfo);
        return headers;
    }

    /**
     * 构建账号秘密验证信息: Basic NTczODAxMWYtOWVkMC00ZjZjLWEyZGMtMTRkNTZkNjIzZTJjOnRlc3QtM2RxOHlxVm4xUnlVT
     *
     * @param userName 账号
     * @param password 密码
     * @return 秘钥信息
     */
    public String buildBasicAuth(String userName, String password) {
        final String data = userName.concat(":").concat(password);
        return "Basic " + new String(Base64.encodeBase64(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String executeRequest(HttpUriRequest httpUriRequest) throws IOException {
        return httpClient.execute(httpUriRequest, responseHandler);
    }

}