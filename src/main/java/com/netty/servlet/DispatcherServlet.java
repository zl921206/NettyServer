package com.netty.servlet;

import java.lang.reflect.InvocationTargetException;

import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSONObject;
import com.netty.annotation.exception.HandlerNotFoundException;
import com.netty.annotation.exception.MissingParameterException;
import com.netty.annotation.exception.RequestMethodNotSupportException;
import com.netty.annotation.exception.resolver.HandlerExceptionResolver;
import com.netty.annotation.support.HandlerResolver;
import com.netty.context.ClassPathApplicationContext;
import com.netty.handler.Handler;
import com.netty.request.HttpServletRequest;
import com.netty.request.WebRequest;
import com.netty.util.HttpResponseUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * 请求处理器：所有客户端请求都会进入此类执行channelRead0()方法
 */
@Slf4j
public class DispatcherServlet extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static HandlerExceptionResolver resolver = null;// 统一的异常处理器

    static {
        try {
            ClassPathApplicationContext context = ClassPathApplicationContext.getInstance();
            resolver = context.getResolver();
        } catch (Exception e) {
            log.error("获取全局异常处理器失败.", e);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        WebRequest request = new HttpServletRequest(req);
        String message = "";
        HttpResponseStatus status = null;
        Handler handler = null;
        String contentType = "application/json";
        try {
            HandlerResolver hr = HandlerResolver.getInstance();
            handler = hr.resorveHandler(request);
            Object[] args = hr.resolveHandlerArguments(handler, request, ctx);// 绑定参数
            Object res = null;
            try {
                res = handler.invoke(args);
            } catch (Exception e) {
                if (resolver != null) {
                    if (e instanceof InvocationTargetException) {
                        e = (Exception) e.getCause();
                    }
                    resolver.resolveException(request, handler, ctx, e);
                    return;
                }
                throw e;
            }
            if (handler.isShakehands()) {// 握手请求不能响应消息
                return;
            }
            if (res != null) {
                if (handler.isResponseBody()) {
                    message = JSONObject.toJSONString(res);
                } else {
                    contentType = "text/html;charset=UTF-8";
                    message = res.toString();
                }
            }
            status = HttpResponseStatus.OK;
        } catch (HandlerNotFoundException e) {
            e.printStackTrace();
            status = HttpResponseStatus.NOT_FOUND;
            message = e.getMessage();
        } catch (RequestMethodNotSupportException e) {
            e.printStackTrace();
            status = HttpResponseStatus.METHOD_NOT_ALLOWED;
            message = e.getMessage();
        } catch (MissingParameterException e) {
            e.printStackTrace();
            status = HttpResponseStatus.BAD_REQUEST;
            message = e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("服务器内部错误...clause=" + e.getMessage(), e);
            status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
            message = e.getMessage();
        }
        HttpResponseUtils.sendHttpResponse(ctx, status, message, contentType);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
