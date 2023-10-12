package com.spring.model;

import com.spring.contant.ScopeType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 保存一个类上面的注解信息，避免重复解析
 *
 * @author 郭建勇
 * @date 2023/10/11
 **/
@Data
@AllArgsConstructor
public class BeanDefinition {

    private Class clazz;

    private ScopeType scope;

    public boolean isSingleton() {
        return scope == ScopeType.SINGLETON;
    }


}
