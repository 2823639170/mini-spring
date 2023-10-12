package com.gjy.service;

import com.spring.Interface.BeanPostProcessor;
import com.spring.annotation.Component;
import com.spring.annotation.ComponentScan;

/**
 * @author 郭建勇
 * @date 2023/10/12
 **/
@Component
public class TestBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("BeanPostProcessor before method -------- " + bean.getClass().getName());
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("BeanPostProcessor after method -------- " + bean.getClass().getName());
        return bean;
    }
}
