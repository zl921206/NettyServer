package com.netty.context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import com.netty.annotation.exception.resolver.HandlerExceptionResolver;
import com.netty.annotation.support.HandlerResolver;
import com.netty.util.Assert;
import com.netty.util.ClassUtils;

/**
 * 上下文 控制器 异常解析处理器 初始化
 */
public class ClassPathApplicationContext {
	private static volatile ClassPathApplicationContext context;
	private HandlerExceptionResolver resolver;// 统一的异常处理器
	private Map<String, String> propMap = new HashMap<String, String>();
	public final String CONTROLLER_PACKAGE_KEY = "web.package";
	public static final String EXCEPTION_RESOLVER_KEY = "web.exceptionResolver";
	private static boolean isInit = false;

	public static ClassPathApplicationContext getInstance() {
		if (context == null) {
			synchronized (ClassPathApplicationContext.class) {
				if (context == null) {
					context = new ClassPathApplicationContext();
				}
			}
		}

		return context;
	}

	private ClassPathApplicationContext() {
	}

	/**
	 * 属性文件类路径，多条路径以英文逗号隔开
	 * 
	 * @param propertyPath
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void start(String propertyPath) throws FileNotFoundException, IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		if (isInit) {
			return;
		}
		Assert.notEmpty(propertyPath, "propertyPath must not be empty.");
		String[] paths = propertyPath.split(",");
		String classPath = ClassPathApplicationContext.class.getResource("/").getPath();
		Properties prop = null;
		Set<String> keys = null;
		String value = null;
		for (String path : paths) {
			prop = new Properties();
			prop.load(new FileInputStream(classPath + File.separator + path));
			keys = prop.stringPropertyNames();
			for (String key : keys) {
				value = prop.getProperty(key);
				propMap.put(key, value);
			}
		}
		// 初始化异常解析处理器
		initResolver();
		// 初始化控制器
		initControllers();
		isInit = true;
	}

	/**
	 * 扫描 controller
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void initControllers()
			throws FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		String pack = this.getProperty(CONTROLLER_PACKAGE_KEY);
		Assert.notEmpty(pack, CONTROLLER_PACKAGE_KEY + " must config");
		Set<Class<?>> classes = ClassUtils.getClasses(pack);
		HandlerResolver hr = HandlerResolver.getInstance();
		hr.init(classes);
	}

	/**
	 * 获取属性值
	 * @param key
	 * @return
	 */
	public String getProperty(String key) {
		return propMap.get(key);
	}

	/**
	 * 初始化异常处理器
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void initResolver() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String pack = this.getProperty(EXCEPTION_RESOLVER_KEY);
		if (pack == null || pack.length() == 0) {
			return;
		}
		Class<?> cls = Thread.currentThread().getContextClassLoader().loadClass(pack);
		if (!HandlerExceptionResolver.class.isAssignableFrom(cls)) {
			throw new IllegalStateException("class '" + cls
					+ "' is not a sub class of com.netty.annotation.exception.resolver.HandlerExceptionResolver");
		}
		this.resolver = (HandlerExceptionResolver) cls.newInstance();
	}

	public HandlerExceptionResolver getResolver() {
		return resolver;
	}

}
