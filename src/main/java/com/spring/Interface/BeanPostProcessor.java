package com.spring.Interface;

/**
 * spring会特别把实现BeanPostProcessor的接口的类单独处理
 * 所写这个实现这个的类一定是单例
 * 然后他会在创建其他单例类之前，优先先先将这些接口全部创建
 * 然后在创建其他单例类之前，会执行下面两个方法
 *
 * 经过我的尝试，即使有多个BeanPostProcessor，
 * 这些类都不会这些这些方法
 * 并且BeanPostProcessor依赖的类也不会执行这些方法
 *
 * @author 郭建勇
 * @date 2023/10/11
 **/
public interface BeanPostProcessor {

    /**
     * 会在createBean方法调用initializingBean的afterProperty方法前调用
     * @param bean
     * @param beanName
     * @return
     */
    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    /**
     * 会在createBean方法调用initializingBean的afterProperty方法之后调用
     * @param bean
     * @param beanName
     * @return
     */
    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

}
