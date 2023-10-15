package com.spring.aop.model;



import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author 郭建勇
 * @date 2023/10/14
 **/
@Data
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


}
