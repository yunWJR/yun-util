package com.yun.util.apilog.advice;

import com.yun.util.apilog.ApiData;
import com.yun.util.apilog.ApiDataUtil;
import com.yun.util.apilog.ApiLogProperties;
import com.yun.util.apilog.annotations.ApiLogAnnotationsUtil;
import com.yun.util.apilog.annotations.ApiLogFiledStatus;
import com.yun.util.common.JsonUtil;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author yun
 * created_time 2019/8/30 13:37.
 */

@RestControllerAdvice
public class ApiDataRequestBodyAdvice implements RequestBodyAdvice {

    private final ApiLogProperties apiLogProperties;

    public ApiDataRequestBodyAdvice(ApiLogProperties apiLogProperties) {
        this.apiLogProperties = apiLogProperties;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        return inputMessage;
    }

    /**
     * 记录请求数据
     * @param body
     * @param inputMessage
     * @param parameter
     * @param targetType
     * @param converterType
     * @return
     */
    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {

        ApiData apiData = ApiDataUtil.getAdviceData();
        if (apiData == null) {
            apiData = ApiData.newItem();
        }

        if (apiData.getStatus() == null) {
            ApiLogFiledStatus status = ApiLogAnnotationsUtil.getFiledStatus(parameter.getMethod(), apiLogProperties);
            apiData.setStatus(status);
        }

        // 保存 body
        if (apiData.getStatus().isHeader()) {
            boolean isJsonBody = false;
            if (inputMessage.getHeaders() != null) {
                List<String> ctHeaders = inputMessage.getHeaders().get("Content-Type");
                if (ctHeaders != null && ctHeaders.size() > 0) {
                    for (String ctHeader : ctHeaders) {
                        if (ApiDataUtil.canToJson(ctHeader)) {
                            isJsonBody = true;
                            break;
                        }
                    }
                }
            }

            if (isJsonBody) {
                apiData.setBody(JsonUtil.toStr(body));
            } else {
                apiData.setBody(body.toString());
            }
        }

        ApiDataUtil.saveAdviceData(apiData);

        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                  Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }
}
