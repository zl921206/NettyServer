package com.netty.util;

import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Locale;

public class BeanUtils {

	/**
	 * Check if the given type represents a "simple" property: a primitive, a String
	 * or other CharSequence, a Number, a Date, a URI, a URL, a Locale, a Class, or
	 * a corresponding array.
	 * <p>
	 * Used to determine properties to check for a "simple" dependency-check.
	 * 
	 * @param clazz
	 *            the type to check
	 * @return whether the given type represents a "simple" property
	 * @see org.springframework.beans.factory.support.RootBeanDefinition#DEPENDENCY_CHECK_SIMPLE
	 * @see org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#checkDependencies
	 */
	public static boolean isSimpleProperty(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return isSimpleValueType(clazz) || (clazz.isArray() && isSimpleValueType(clazz.getComponentType()));
	}

	/**
	 * Check if the given type represents a "simple" value type: a primitive, a
	 * String or other CharSequence, a Number, a Date, a URI, a URL, a Locale or a
	 * Class.
	 * 
	 * @param clazz
	 *            the type to check
	 * @return whether the given type represents a "simple" value type
	 */
	public static boolean isSimpleValueType(Class<?> clazz) {
		return (ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.isEnum() || CharSequence.class.isAssignableFrom(clazz)
				|| Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz) || URI.class == clazz
				|| URL.class == clazz || Locale.class == clazz || Class.class == clazz);
	}

}
