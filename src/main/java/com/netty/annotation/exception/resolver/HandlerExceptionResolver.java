package com.netty.annotation.exception.resolver;

import com.netty.handler.Handler;
import com.netty.request.WebRequest;

import io.netty.channel.ChannelHandlerContext;

/**
 * 统一的异常处理器
 * 
 * @author Administrator
 *
 */
public interface HandlerExceptionResolver {
	public void resolveException(WebRequest request, Handler handler, ChannelHandlerContext ctx, Exception e);
}
