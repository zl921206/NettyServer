package com.netty.request;

import java.util.Map;
import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * web请求接口
 */
public interface WebRequest {
	/**
	 * Return the request header of the given name, or {@code null} if none.
	 * <p>Retrieves the first header value in case of a multi-value header.
	 * @since 3.0
	 * @see javax.servlet.http.HttpServletRequest#getHeader(String)
	 */
	String getHeader(String headerName);

	/**
	 * Return the request header values for the given header name,
	 * or {@code null} if none.
	 * <p>A single-value header will be exposed as an array with a single element.
	 * @since 3.0
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(String)
	 */
	String[] getHeaderValues(String headerName);
	
	/**
	 * Return the request parameter of the given name, or {@code null} if none.
	 * <p>Retrieves the first parameter value in case of a multi-value parameter.
	 * @see javax.servlet.http.HttpServletRequest#getParameter(String)
	 */
	String getParameter(String paramName);

	/**
	 * Return the request parameter values for the given parameter name,
	 * or {@code null} if none.
	 * <p>A single-value parameter will be exposed as an array with a single element.
	 * @see javax.servlet.http.HttpServletRequest#getParameterValues(String)
	 */
	String[] getParameterValues(String paramName);
	
	/**
	 * Return a immutable Map of the request parameters, with parameter names as map keys
	 * and parameter values as map values. The map values will be of type String array.
	 * <p>A single-value parameter will be exposed as an array with a single element.
	 * @see javax.servlet.http.HttpServletRequest#getParameterMap()
	 */
	Map<String, String[]> getParameterMap();
	
	/**
	 * 返回请求方法
	 * @return
	 */
	RequestMethod getHttpMethod();
	
	/**

	 * 返回相应cookie值
	 * @param cookieName
	 * @return
	 */
	String getCookie(String cookieName);

	/**
	 * Return the underlying native request object, if available.
	 * @see javax.servlet.http.HttpServletRequest
	 * @see javax.portlet.ActionRequest
	 * @see javax.portlet.RenderRequest
	 */
	FullHttpRequest getNativeRequest();
	
	String getMethod();
	
	String getRequestURI();
}
