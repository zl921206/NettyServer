package com.netty.util;

import java.lang.reflect.Modifier;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

/**
 * 获取类中方法的参数名称工具类
 */
public class MethodParamNameUtils {

    /**
     * 获取类中方法的参数名称
     *
     * @param clzz
     * @param declaredMethodName 方法名
     * @return
     * @throws NotFoundException
     */
    public static String[] getParamNames(Class<?> clazz, String declaredMethodName) throws NotFoundException {
        Assert.notNull(clazz, "clazz must not be null");
        Assert.notNull(declaredMethodName, "declaredMethodName must not be null");
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.get(clazz.getName());
        CtMethod cm = cc.getDeclaredMethod(declaredMethodName);
        // 使用javaassist的反射方法获取方法的参数名
        MethodInfo methodInfo = cm.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        if (attr == null) {
            return new String[0];
        }
        String[] paramNames = new String[cm.getParameterTypes().length];
        int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
        for (int i = 0; i < paramNames.length; i++)
            paramNames[i] = attr.variableName(i + pos);

        return paramNames;
    }
}
