package com.netty.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 反射帮助类
 * @author Administrator
 */
public class ReflectionUtils {

	/**
	 * 判断某个方法是不是getter方法
	 * 
	 * @param method
	 * @return
	 */
	public boolean isGetter(Method method) {
		if (!method.getName().startsWith("get"))
			return false;
		if (method.getParameterTypes().length == 0)
			return true;
		if (!void.class.equals(method.getReturnType()))
			return false;
		return true;
	}

	/**
	 * 判断某个方法是不是setter方法
	 * 
	 * @param method
	 * @return
	 */
	public boolean isSetter(Method method) {
		if (!method.getName().startsWith("set"))
			return false;
		if (method.getParameterTypes().length != 1)
			return false;
		return true;
	}

	/**
	 * 获取所有getter方法
	 * @param clzz
	 * @return
	 */
	public <T> List<MethodInfo> getterMethodInfoList(Class<T> clzz){
		Method[] methods = clzz.getDeclaredMethods();
		if(methods == null || methods.length == 0) {
			return null;
		}
		List<MethodInfo> list = new ArrayList<MethodInfo>();
		for(Method method: methods) {
			if(!isGetter(method)) {
				continue;
			}
			String name = getterName(method.getName());
			list.add(new MethodInfo(method, name));
		}
	
		return list;
	}
	
	/**
	 * 获取所有getter方法
	 * @param clzz
	 * @return
	 */
	public <T> List<MethodInfo> setterMethodInfoList(Class<T> clzz){
		Method[] methods = clzz.getDeclaredMethods();
		if(methods == null || methods.length == 0) {
			return null;
		}
		List<MethodInfo> list = new ArrayList<MethodInfo>();
		for(Method method: methods) {
			if(!isSetter(method)) {
				continue;
			}
			String name = getterName(method.getName());
			list.add(new MethodInfo(method, name));
		}
		
		return list;
	}
	
	public String getterName(String getterMethodName) {
		Assert.notEmpty(getterMethodName, "getterMethodName must not be empty.");
		StringBuilder sb = new StringBuilder(getterMethodName);
		return Character.toLowerCase(sb.charAt(3)) + sb.substring(4);
		
	}
	
	public String setterName(String setterMethodName) {
		Assert.notEmpty(setterMethodName, "setterMethodName must not be empty.");
		StringBuilder sb = new StringBuilder(setterMethodName);
		return Character.toLowerCase(sb.charAt(3)) + sb.substring(4);
		
	}

	public static class MethodInfo {
		private Method mehtod;
		private String name;// 方法名对应pojo中的名称,如setterAge方法对应age

		public MethodInfo() {
		}
		
		public MethodInfo(Method mehtod, String name) {
			super();
			this.mehtod = mehtod;
			this.name = name;
		}

		public Method getMehtod() {
			return mehtod;
		}

		public void setMehtod(Method mehtod) {
			this.mehtod = mehtod;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "MethodInfo [mehtod=" + mehtod.getName() + ", name=" + name + "]";
		}
		
	}
}
