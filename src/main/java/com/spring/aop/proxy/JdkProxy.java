package com.spring.aop.proxy;

import com.spring.aop.model.ProceedingJoinPoint;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author 郭建勇
 * @date 2023/10/15
 **/
@Data
@AllArgsConstructor
public class JdkProxy {

    /**
     * 目标类的class对象
     */
    private Class targetClass;
    /**
     * 目标类的实例对象
     */
    private Object targetBean;
    /**
     * 目标方法
     */
    private Method targetMethod;
    /**
     * Aop类的class对象
     */
    private Class AopClass;
    /**
     * Aop方法
     */
    private Method AopMethod;


    public Object getProxy() {
        Object proxy = Proxy.newProxyInstance(targetClass.getClassLoader(), targetClass.getInterfaces(), new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals(targetMethod.getName())) {
                    ProceedingJoinPoint proceedingJoinPoint = new ProceedingJoinPoint(targetBean, method, args);
                    Object newInstance = AopClass.newInstance();
                    return AopMethod.invoke(newInstance, proceedingJoinPoint);
                } else {
                    return method.invoke(targetBean, args);
                }
            }
        });
        return proxy;
    }
}