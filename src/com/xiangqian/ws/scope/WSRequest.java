package com.xiangqian.ws.scope;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket请求参数
 *
 * @author xiangqian
 * @date 16:35 2020/01/01
 */
public class WSRequest implements HttpRequest {

    private String uri;
    private Map<String, String> parameterMap;

    public WSRequest(String uri) {
        this.uri = uri;
        init();
    }

    private void init() {
        parameterMap = new HashMap<>();
        int index = -1;
        if ((index = uri.indexOf("?")) > 0) {
            String[] parameterKVArr = uri.substring(index + 1).split("&");
            String[] parameterKV = null;
            for (String parameterKVStr : parameterKVArr) {
                parameterKV = parameterKVStr.split("=");
                parameterMap.put(parameterKV[0], parameterKV.length > 1 ? parameterKV[1] : null);
            }
        }
    }

    @Override
    public HttpMethod getMethod() {
        return null;
    }

    @Override
    public HttpMethod method() {
        return null;
    }

    @Override
    public HttpRequest setMethod(HttpMethod httpMethod) {
        return null;
    }

    @Override
    public String getUri() {
        return null;
    }

    @Override
    public String uri() {
        return null;
    }

    @Override
    public HttpRequest setUri(String s) {
        return null;
    }

    @Override
    public HttpVersion getProtocolVersion() {
        return null;
    }

    @Override
    public HttpVersion protocolVersion() {
        return null;
    }

    @Override
    public HttpRequest setProtocolVersion(HttpVersion httpVersion) {
        return null;
    }

    @Override
    public HttpHeaders headers() {
        return null;
    }

    @Override
    public DecoderResult getDecoderResult() {
        return null;
    }

    @Override
    public DecoderResult decoderResult() {
        return null;
    }

    @Override
    public void setDecoderResult(DecoderResult decoderResult) {

    }

    public String getParameter(String name) {
        return this.parameterMap.get(name);
    }

    public Map<String, String> getParameters() {
        return this.parameterMap;
    }
}
