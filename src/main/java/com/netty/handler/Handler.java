package com.netty.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.netty.util.MethodParamNameUtils;
import javassist.NotFoundException;

public class Handler {
	private final Object controller;
	private final Method method;
	//private final RequestMapping requestMapping;
	private final boolean isResponseBody;// 是否包含ResponseBody注解
	private volatile Annotation[][] parameterAnnotations;// 方法参数中的注解
	private String[] paramNames = null;// 方法参数名
	private final boolean isShakehands;// 是否包含Shakehands注解

	public Handler(Object controller, Method method, 
			boolean isResponseBody, boolean isShakehands) {
		super();
		this.controller = controller;
		this.method = method;
		this.isResponseBody = isResponseBody;
		this.isShakehands = isShakehands;
	}

	public Object invoke(Object[] args)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return this.method.invoke(controller, args);
	}

	public boolean isResponseBody() {
		return isResponseBody;
	}

	/**
	 * Return the annotations associated with the specific method/constructor
	 * parameter.
	 */
	public Annotation[] getParameterAnnotations(int parameterIndex) {
		if (this.parameterAnnotations == null) {
			parameterAnnotations = this.method.getParameterAnnotations();
		}
		return this.parameterAnnotations[parameterIndex];
	}

	public Method getMethod() {
		return method;
	}

	@Override
	public String toString() {
		return this.method.getName();
	}

	public String[] getParamNames() throws NotFoundException {
		if (paramNames == null) {
			paramNames = MethodParamNameUtils.getParamNames(this.controller.getClass(), this.method.getName());
		}
		return paramNames;
	}

	public boolean isShakehands() {
		return isShakehands;
	}

	
}
