package com.spring.Interface;

/**
 * @author 郭建勇
 * @date 2023/10/11
 **/
public interface BeanNameAware {

    /**
     * spring在生成bean对象的时候，会检查这个类是否会实现BeanNameAware
     * 如果实现，则调用这个方法，并将beamName（bean的名字）传入这个方法中
     * @param beamName
     */
    void setBeamName(String beamName);


}
