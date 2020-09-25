package com.netty.annotation.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.netty.annotation.exception.HandlerNotFoundException;
import com.netty.annotation.exception.MissingParameterException;
import com.netty.annotation.exception.RequestMethodNotSupportException;
import com.netty.annotation.ext.Shakehands;
import com.netty.annotation.util.AnnotationUtils;
import com.netty.context.ClassPathApplicationContext;
import com.netty.converter.Converter;
import com.netty.converter.StringToBooleanConverter;
import com.netty.converter.StringToCharacterConverter;
import com.netty.converter.StringToNumberConverterFactory;
import com.netty.handler.Handler;
import com.netty.request.WebRequest;
import com.netty.util.BeanUtils;
import com.netty.util.ClassUtils;
import com.netty.util.ReflectionUtils;
import com.netty.util.ReflectionUtils.MethodInfo;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Handler 解析处理
 */
public class HandlerResolver {
	private final Map<String, Handler> handlerMap = new HashMap<String, Handler>();
	private static volatile HandlerResolver handlerResolver;
	private final Map<String, List<RequestMethod>> handlerMethods = new HashMap<String, List<RequestMethod>>();
	public static Object UNRESOLVED = new Object();
	@SuppressWarnings("rawtypes")
	private static Map<Class, Converter> convertsMap = new HashMap<Class, Converter>();// 各种类型转换器

	static {
		StringToNumberConverterFactory factory = new StringToNumberConverterFactory();
		convertsMap.put(byte.class, factory.getConverter(Byte.class));
		convertsMap.put(Byte.class, convertsMap.get(byte.class));
		convertsMap.put(short.class, factory.getConverter(Short.class));
		convertsMap.put(Short.class, convertsMap.get(short.class));
		convertsMap.put(float.class, factory.getConverter(Float.class));
		convertsMap.put(Float.class, convertsMap.get(float.class));
		convertsMap.put(int.class, factory.getConverter(Integer.class));
		convertsMap.put(Integer.class, convertsMap.get(int.class));
		convertsMap.put(double.class, factory.getConverter(Double.class));
		convertsMap.put(Double.class, convertsMap.get(double.class));
		convertsMap.put(long.class, factory.getConverter(Long.class));
		convertsMap.put(Long.class, convertsMap.get(long.class));
		factory = null;
		convertsMap.put(boolean.class, new StringToBooleanConverter());
		convertsMap.put(Boolean.class, convertsMap.get(boolean.class));
		convertsMap.put(char.class, new StringToCharacterConverter());
		convertsMap.put(Character.class, convertsMap.get(char.class));
	}

	private HandlerResolver() {

	}

	public static HandlerResolver getInstance() {
		if (handlerResolver == null) {
			synchronized (HandlerResolver.class) {
				if (handlerResolver == null) {
					handlerResolver = new HandlerResolver();
				}
			}
		}
		return handlerResolver;
	}

	/**
	 * 解析所有handler
	 * 
	 * @param classes
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void init(Set<Class<?>> classes) throws InstantiationException, IllegalAccessException {
		String[] values = null;
		List<RequestMethod> list = null;
		RequestMethod[] methods = null;
		RequestMapping mapping = null;
		String key = null;
		String context = "";
		String path = null;
		boolean isResponseBody = false;
		boolean isShakehands = false;
		Handler handler = null;
		for (Class<?> cls : classes) {
			if (!cls.isAnnotationPresent(Controller.class)) {
				continue;
			}
			context = "";
			mapping = (RequestMapping) AnnotationUtils.getClassAnnotation(cls, RequestMapping.class);
			if (mapping != null && (context = mapping.value()[0]) != null) {
				context = formatUri(context);
			}
			Object controller = cls.newInstance();
			for (Method method : cls.getDeclaredMethods()) {
				if (!method.isAnnotationPresent(RequestMapping.class)) {
					continue;
				}
				mapping = method.getAnnotation(RequestMapping.class);
				values = mapping.value();
				if (values == null || values.length == 0) {
					throw new RuntimeException("RequestMapping value is empty");
				}
				for (String value : values) {
					value = formatUri(value);
					path = context + value;

					isResponseBody = method.isAnnotationPresent(ResponseBody.class);
					isShakehands = method.isAnnotationPresent(Shakehands.class);
					handler = new Handler(controller, method, isResponseBody, isShakehands);
					methods = mapping.method();
					if (methods.length == 0) {
						methods = RequestMethod.values();
					}
					list = handlerMethods.get(path);
					if (list == null) {
						list = new ArrayList<RequestMethod>();
					}
					for (RequestMethod rm : methods) {
						if (list.contains(rm)) {
							throw new RuntimeException(
									"handler已重复定义[RequestMapping=" + value + ",RequestMethod=" + rm + "].");
						}
						list.add(rm);
						key = path + "_" + rm;
						handlerMap.put(key, handler);
					}
					handlerMethods.put(path, list);
				}

			}
		}
	}

	private String formatUri(String path) {
		String reg = "\\/+";
		if (path.startsWith(reg)) {
			path = path.replace(reg, "/");
		} else {
			path = "/" + path;
		}
		return path;
	}

	/**
	 * 根据请求获取handler
	 * 
	 * @param req
	 * @return
	 * @throws RequestMethodNotSupportException
	 * @throws HandlerNotFoundException
	 */
	public Handler resorveHandler(WebRequest req) throws RequestMethodNotSupportException, HandlerNotFoundException {

		String uri = req.getRequestURI();
		String path = uri + "_" + req.getMethod();
		Handler handler = this.handlerMap.get(path);
		if (handler == null) {
			if (handlerMethods.containsKey(uri)) {
				throw new RequestMethodNotSupportException(
						"The request method '" + req.getMethod() + "' does not sopport this mapping[" + uri + "].");
			}
			throw new HandlerNotFoundException("Can not find handler for this mapping[" + uri + "].");
		}
		return handler;
	}

	/**
	 * 参数绑定
	 * 
	 * @param handler
	 * @param webRequest
	 * @return
	 * @throws Exception
	 */
	public Object[] resolveHandlerArguments(Handler handler, WebRequest webRequest, Object... optionals)
			throws Exception {

		Class<?>[] paramTypes = handler.getMethod().getParameterTypes();
		Object[] args = new Object[paramTypes.length];

		for (int i = 0; i < args.length; i++) {
			String paramName = null;
			String headerName = null;
			String cookieName = null;
			boolean required = false;
			String defaultValue = null;
			String attrName = null;
			int annotationsFound = 0;
			Annotation[] paramAnns = handler.getParameterAnnotations(i);

			for (Annotation paramAnn : paramAnns) {
				if (RequestParam.class.isInstance(paramAnn)) {
					RequestParam requestParam = (RequestParam) paramAnn;
					paramName = requestParam.value();
					required = requestParam.required();
					defaultValue = parseDefaultValueAttribute(requestParam.defaultValue());
					annotationsFound++;
				} else if (RequestHeader.class.isInstance(paramAnn)) {
					RequestHeader requestHeader = (RequestHeader) paramAnn;
					headerName = requestHeader.value();
					required = requestHeader.required();
					defaultValue = parseDefaultValueAttribute(requestHeader.defaultValue());
					annotationsFound++;
				} else if (CookieValue.class.isInstance(paramAnn)) {
					CookieValue cookieValue = (CookieValue) paramAnn;
					cookieName = cookieValue.value();
					required = cookieValue.required();
					defaultValue = parseDefaultValueAttribute(cookieValue.defaultValue());
					annotationsFound++;
				} else if (Value.class.isInstance(paramAnn)) {
					defaultValue = ((Value) paramAnn).value();
				}
			}

			if (annotationsFound > 1) {
				throw new IllegalStateException("Handler parameter annotations are exclusive choices - "
						+ "do not specify more than one such annotation on the same parameter: " + handler);
			}

			if (annotationsFound == 0) {// 不使用注解的参数绑定
				Object argValue = resolveCommonArgument(paramTypes[i], webRequest);
				if (argValue != UNRESOLVED) {
					args[i] = argValue;
				} else if (defaultValue != null) {
					args[i] = defaultValue;
				} else if (BeanUtils.isSimpleProperty(paramTypes[i])) {
					paramName = "";
				} else {
					attrName = "";
				}
			}

			if (paramName != null) {
				paramName = getRequiredParameterName(handler, i, paramName);
				args[i] = resolveRequestParam(paramName, required, defaultValue, paramTypes[i], webRequest);
			} else if (headerName != null) {
				headerName = getRequiredParameterName(handler, i, headerName);
				args[i] = resolveRequestHeader(headerName, required, defaultValue, paramTypes[i], webRequest);
			} else if (cookieName != null) {
				cookieName = getRequiredParameterName(handler, i, cookieName);
				args[i] = resolveCookieValue(cookieName, required, defaultValue, paramTypes[i], webRequest);
			} else if (attrName != null) {// 复杂类型参数绑定
				args[i] = resolveObject(paramTypes[i], webRequest, optionals);
			} else if (defaultValue != null) {
				args[i] = resolveValue(paramTypes[i], defaultValue);
			}
		}

		return args;
	}

	@SuppressWarnings({ "rawtypes" })
	private Object resolveValue(Class paramTypes, String key) {
		ClassPathApplicationContext context = ClassPathApplicationContext.getInstance();
		Object res = context.getProperty(key);
		try {
			res = convertTypeIfNeccesary(paramTypes, res);
		} catch (Exception e) {
			throw new IllegalStateException("invalid value for property key '" + key + "':" + res);
		}
		return res;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object resolveObject(Class paramTypes, WebRequest webRequest, Object... optionals)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		if (Map.class.isAssignableFrom(paramTypes)) {

		} else if (List.class.isAssignableFrom(paramTypes)) {

		}
		for (Object obj : optionals) {
			if (paramTypes.isAssignableFrom(obj.getClass()) || paramTypes == obj.getClass()) {
				return obj;
			}
		}
		ReflectionUtils util = new ReflectionUtils();
		List<MethodInfo> list = util.setterMethodInfoList(paramTypes);
		Object obj = paramTypes.newInstance();
		Method method = null;
		Class<?> clzz = null;
		Object arg = null;
		for (MethodInfo info : list) {
			method = info.getMehtod();
			clzz = method.getParameterTypes()[0];
			if (!BeanUtils.isSimpleValueType(clzz)) {
				resolveObject(clzz, webRequest, optionals);
			}
			String value = webRequest.getParameter(info.getName());
			try {
				arg = convertTypeIfNeccesary(clzz, value);
			} catch (Exception e) {
				throw new IllegalStateException("invalid value for parameter '" + info.getName() + "':" + value);
			}
			info.getMehtod().invoke(obj, arg);// 调用对象setter方法
		}

		return obj;
	}

	private String getRequiredParameterName(Handler handler, int i, String paramName) throws NotFoundException {
		if (paramName.length() == 0) {
			paramName = handler.getParamNames()[i];
		}
		return paramName;
	}

	private String parseDefaultValueAttribute(String value) {
		value = "".equals(value) ? null : value;
		return value;
	}

	protected Object resolveCommonArgument(Class<?> paramType, WebRequest webRequest) throws Exception {

		// Invoke custom argument resolvers if present...
		/*
		 * if (this.customArgumentResolvers != null) { for (WebArgumentResolver
		 * argumentResolver : this.customArgumentResolvers) { Object value =
		 * argumentResolver.resolveArgument(methodParameter, webRequest); if (value !=
		 * WebArgumentResolver.UNRESOLVED) { return value; } } }
		 */

		// Resolution of standard parameter types...
		Object value = resolveStandardArgument(paramType, webRequest);
		if (value != UNRESOLVED && !ClassUtils.isAssignableValue(paramType, value)) {
			throw new IllegalStateException("Standard argument type [" + paramType.getName()
					+ "] resolved to incompatible value of type [" + (value != null ? value.getClass() : null)
					+ "]. Consider declaring the argument type in a less specific fashion.");
		}
		return value;
	}

	protected Object resolveStandardArgument(Class<?> parameterType, WebRequest webRequest) throws Exception {
		if (WebRequest.class.isAssignableFrom(parameterType)) {
			return webRequest;
		}
		return UNRESOLVED;
	}

	@SuppressWarnings({ "unchecked" })
	private Object resolveRequestParam(String paramName, boolean required, String defaultValue, Class<?> parameterType,
			WebRequest webRequest) throws Exception {

		if (Map.class.isAssignableFrom(parameterType) && paramName.length() == 0) {
			return resolveRequestParamMap((Class<? extends Map<?, ?>>) parameterType, webRequest);
		}
		Object paramValue = null;
		/*
		 * MultipartRequest multipartRequest =
		 * webRequest.getNativeRequest(MultipartRequest.class); if (multipartRequest !=
		 * null) { List<MultipartFile> files = multipartRequest.getFiles(paramName); if
		 * (!files.isEmpty()) { paramValue = (files.size() == 1 ? files.get(0) : files);
		 * } }
		 */
		if (paramValue == null) {
			String[] paramValues = webRequest.getParameterValues(paramName);
			if (paramValues != null && paramValues.length > 0) {
				paramValue = (parameterType.isArray() ? paramValues : paramValues[0]);
			}
		}
		if (paramValue == null) {
			if (defaultValue != null) {
				paramValue = defaultValue;
			} else if (required) {
				raiseMissingParameterException(paramName, parameterType);
			}
			paramValue = checkValue(paramName, paramValue, parameterType);
		}

		try {
			paramValue = convertTypeIfNeccesary(parameterType, paramValue);
		} catch (Exception e) {
			throw e;
		}
		return paramValue;
	}

	/**
	 * 类型转换
	 * 
	 * @param parameterType
	 * @param paramValue
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object convertTypeIfNeccesary(Class parameterType, Object paramValue) {
		if (paramValue == null) {
			return null;
		}
		if (String.class == parameterType || String[].class == parameterType) {
			return paramValue;
		}
		boolean isArray = parameterType.isArray();
		parameterType = isArray ? parameterType.getComponentType() : parameterType;
		Converter converter = convertsMap.get(parameterType);
		if (converter == null) {
			throw new IllegalArgumentException("can not convert the class type '" + parameterType.getName() + "'.");
		}
		if (isArray) {
			Object[] array = (Object[]) paramValue;
			Object res = Array.newInstance(parameterType, array.length);
			for (int i = 0; i < array.length; i++) {
				Array.set(res, i, converter.convert(array[i]));
			}
			return res;
		} else {
			paramValue = converter.convert(paramValue);
		}
		
		return paramValue;

	}

	private Object resolveRequestHeader(String headerName, boolean required, String defaultValue,
			Class<?> parameterType, WebRequest webRequest) throws Exception {
		Object headerValue = null;
		String[] headerValues = webRequest.getHeaderValues(headerName);
		if (headerValues != null && headerValues.length > 0) {
			headerValue = (headerValues.length == 1 ? headerValues[0] : headerValues);
		}
		if (headerValue == null) {
			if (defaultValue != null) {
				headerValue = defaultValue;
			} else if (required) {
				raiseMissingHeaderException(headerName, parameterType);
			}
			headerValue = checkValue(headerName, headerValue, parameterType);
		}
		try {
			headerValue = convertTypeIfNeccesary(parameterType, headerValue);
		} catch (Exception e) {
			throw new IllegalStateException("invalid value for header '" + headerName + "':" + headerValue);
		}
		return headerValue;
	}

	private Map<String, ?> resolveRequestParamMap(Class<? extends Map<?, ?>> mapType, WebRequest webRequest) {
		Map<String, String[]> parameterMap = webRequest.getParameterMap();
		Map<String, String> result = new LinkedHashMap<String, String>(parameterMap.size());
		for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
			if (entry.getValue().length > 0) {
				result.put(entry.getKey(), entry.getValue()[0]);
			}
		}
		return result;
	}

	private Object resolveCookieValue(String cookieName, boolean required, String defaultValue, Class<?> parameterType,
			WebRequest webRequest) throws Exception {

		Object cookieValue = webRequest.getCookie(cookieName);

		if (cookieValue == null) {
			if (defaultValue != null) {
				cookieValue = defaultValue;
			} else if (required) {
				raiseMissingCookieException(cookieName, parameterType);
			}
			cookieValue = checkValue(cookieName, cookieValue, parameterType);
		}
		try {
			cookieValue = convertTypeIfNeccesary(parameterType, cookieValue);
		} catch (Exception e) {
			throw new IllegalStateException("invalid value for Cookie '" + cookieName + "':" + cookieValue);
		}
		return cookieValue;
	}

	private Object checkValue(String name, Object value, Class<?> paramType) {
		if (value == null) {
			if (boolean.class.equals(paramType)) {
				return Boolean.FALSE;
			} else if (paramType.isPrimitive()) {
				throw new IllegalStateException("Optional " + paramType + " parameter '" + name
						+ "' is not present but cannot be translated into a null value due to being declared as a "
						+ "primitive type. Consider declaring it as object wrapper for the corresponding primitive type.");
			}
		}
		return value;
	}

	protected void raiseMissingParameterException(String paramName, Class<?> paramType) throws Exception {
		throw new MissingParameterException(
				"Required " + paramType.getName() + " parameter '" + paramName + "' is not present");
	}

	protected void raiseMissingHeaderException(String headerName, Class<?> paramType) throws Exception {
		throw new MissingParameterException(
				"Required " + paramType.getName() + " header '" + headerName + "' is not present");
	}

	protected void raiseMissingCookieException(String cookieName, Class<?> paramType) throws Exception {
		throw new MissingParameterException(
				"Required " + paramType.getName() + " cookie '" + cookieName + "' is not present");
	}
}
