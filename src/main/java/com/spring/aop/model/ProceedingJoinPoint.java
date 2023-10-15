package com.spring.aop.model;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author 郭建勇
 * @date 2023/10/14
 **/
@Data
@AllArgsConstructor
public class ProceedingJoinPoint {

    /**
     * 要加强的类对象
     */
    private Object targetObject;
    /**
     * 要加强的方法
     */
    private Method method;
    /**
     * 方法参数
     */
    private Object[] args;

    public Object proceed() {
        try {
            //调用目标方法
//            System.out.println(method.getName());
//            System.out.println(targetObject);
            return method.invoke(targetObject, args);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return null;
    }


}
