package com.gjy.aop;

import com.spring.annotation.Component;
import com.spring.aop.anno.Around;
import com.spring.aop.anno.Aspect;
import com.spring.aop.model.ProceedingJoinPoint;

import javax.swing.text.rtf.RTFEditorKit;
import java.lang.reflect.InvocationTargetException;

/**
 * @author 郭建勇
 * @date 2023/10/14
 **/
@Aspect
@Component
public class TestAop {

    @Around(execution = "com.gjy.service.impl.UserServiceImpl.print")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) {
        Object result = null;
        result = proceedingJoinPoint.proceed();
        System.out.println("==>前置日志通知.......");
        //调用目标对象的目标方法
        result = proceedingJoinPoint.proceed();
        System.out.println("==>返回日志通知.......");
        return result;
    }

}
