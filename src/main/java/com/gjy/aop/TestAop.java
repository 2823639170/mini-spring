package com.gjy.aop;

import com.spring.aop.anno.Around;
import com.spring.aop.anno.Aspect;
import com.spring.aop.model.ProceedingJoinPoint;

import java.lang.reflect.InvocationTargetException;

/**
 * @author 郭建勇
 * @date 2023/10/14
 **/
@Aspect
public class TestAop {

    @Around(value = "com.service.user.print")
    public void around(ProceedingJoinPoint proceedingJoinPoint) {
        System.out.println("--------before--------");
        try {
            proceedingJoinPoint.getMethod().invoke(proceedingJoinPoint.getTargetObject(), proceedingJoinPoint.getArgs());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        System.out.println("--------after--------");

    }

}
