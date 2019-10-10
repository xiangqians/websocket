package com.xq.ws.http;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiangqian
 */
public class WSHttpRequest implements HttpRequest {

    private Map<String, String> parameterMap;

    public WSHttpRequest() {
        super();
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

    protected void setParameter(Map<String, String> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public String getParameter(String name) {
        return this.parameterMap.get(name);
    }

    public Map<String, String> getParameters() {
        return this.parameterMap;
    }
}
