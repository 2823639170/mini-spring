package com.gjy.aop;

import com.spring.annotation.Component;
import com.spring.aop.anno.Around;
import com.spring.aop.anno.Aspect;
import com.spring.aop.model.ProceedingJoinPoint;

/**
 * 测试多次代理
 * @author 郭建勇
 * @date 2023/10/14
 **/
@Aspect
@Component
public class TestAop2 {

    @Around(execution = "com.gjy.service.impl.UserServiceImpl.print")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) {
        Object result = null;
        System.out.println("==>前置日志通知2.......");
        //调用目标对象的目标方法
        result = proceedingJoinPoint.proceed();
        System.out.println("==>返回日志通知2.......");
        return result;
    }

}
