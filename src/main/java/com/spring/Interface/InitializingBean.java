package com.spring.Interface;


/**
 *
 * @author EVA
 */
public interface InitializingBean {

    /**
     * 如果实现这个接口
     * spring会在给属性赋值之后调用这个方法
     */
    void afterPropertySet();

}
