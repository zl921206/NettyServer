package com.netty.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

/**
 * 响应处理
 */
public class HttpResponseUtils {

	public static void sendHttpResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String message,
			String contentType) {
		ByteBuf content = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
		FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
		res.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		f.addListener(ChannelFutureListener.CLOSE);// 关闭连接
	}

	public static void sendHttpResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String message,
			HttpHeaders headers) {
		ByteBuf content = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
		FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
		res.headers().set(headers);
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		f.addListener(ChannelFutureListener.CLOSE);// 关闭连接
	}

}
