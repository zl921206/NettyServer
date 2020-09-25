package com.netty.annotation.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AnnotationUtils {
	
	/**
	 * 获取类中的注解
	 * @param classes
	 * @param annotationClass
	 * @return
	 */
	public static Annotation getClassAnnotation(Class<?> classes,Class<? extends Annotation> annotationClass){
		if(classes.isAnnotationPresent(annotationClass)){
			return classes.getAnnotation(annotationClass);
		}
		return null;
	}
	
	/**
	 * 获取方法中的注解
	 * @param classes
	 * @param annotationClass
	 * @return
	 */
	public static List<Annotation> getMethodAnnotation(Class<?> classes,Class<? extends Annotation> annotationClass){
		List<Annotation> list = new ArrayList<Annotation>();
		for(Method method : classes.getDeclaredMethods()){
			if(!method.isAnnotationPresent(annotationClass)){
				continue;
			}
			list.add(method.getAnnotation(annotationClass));
		}
		return list;
	}
}
