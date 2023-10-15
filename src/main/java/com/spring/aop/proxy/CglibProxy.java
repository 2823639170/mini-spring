package com.spring.aop.proxy;

import com.spring.transactional.TransactionalManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Set;

/**
 * @author 郭建勇
 * @date 2023/10/16
 **/
@Data
@AllArgsConstructor
public class CglibProxy {

    /**
     * 要代理的类，一定是impl
     */
    Class<?> targetClass;
    /**
     * 要代理的对象
     */
    Object targetObject;

    /**
     * 哪些方法需要被代理
     */
    Set<Method> transactionalMethodSet;

    /**
     * 获取代理对象
     *
     * @return
     */
    public Object getProxyInstance() {

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                // 下面这行代码加上会出现StackOverFlow应该是o.toString方法被递归调用了
//                System.out.println(o + "对象的" + method.getName() + "方法开始cglib代理！");
                // 获取connect，并且设置为手动提交
                Object result = null;
                if (transactionalMethodSet.contains(method)) {
                    System.out.println(method.getName() + "方法开始cglib代理！");
                    Connection connect = TransactionalManager.getThreadLocalConnect();
                    connect.setAutoCommit(false);
                    // 调用目标类方法
                    try {
                        result = method.invoke(targetObject, args);
                        connect.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                        connect.rollback();
                    } finally {

                    }
                } else {
                    result = method.invoke(targetObject, args);
                }
                return result;
            }
        });
        return enhancer.create();
    }


}
