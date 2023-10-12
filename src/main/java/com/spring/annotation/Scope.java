package com.spring.annotation;

import com.spring.contant.ScopeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 要来表示一个类是不是需要单例创建
 *
 * @author EVA
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scope {

    ScopeType value() default ScopeType.SINGLETON;

}
