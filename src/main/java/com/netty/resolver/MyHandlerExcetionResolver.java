package com.netty.resolver;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.netty.annotation.exception.resolver.HandlerExceptionResolver;
import com.netty.handler.Handler;
import com.netty.request.WebRequest;
import com.netty.util.HttpResponseUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * 统一的异常处理器
 *
 * @author zhanglei
 */
@Slf4j
public class MyHandlerExcetionResolver implements HandlerExceptionResolver {

    @Override
    public void resolveException(WebRequest request, Handler handler, ChannelHandlerContext ctx, Exception e) {
        e.printStackTrace();
        log.warn("异常处理器捕获到异常了~");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("error", "程序发生异常，被统一的异常处理器捕获了");
        String message = JSONObject.toJSONString(map);
        HttpResponseUtils.sendHttpResponse(ctx, HttpResponseStatus.OK, message, "application/json");
    }

}