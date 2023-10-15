package com.gjy;

import com.gjy.config.AppConfig;
import com.gjy.controller.UserController;
import com.gjy.service.OrderService;
import com.gjy.service.UserService;
import com.gjy.service.impl.OrderServiceImpl;
import com.gjy.service.impl.UserServiceImpl;
import com.spring.GjySpringApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author 郭建勇
 * @date 2023/10/11
 **/
public class ApplicationStart {

    public static void main(String[] args) {

        GjySpringApplicationContext applicationContext = new GjySpringApplicationContext(AppConfig.class);
//        System.out.println(applicationContext.getBean("userService"));
//        System.out.println(applicationContext.getBean("userService"));
//        System.out.println(applicationContext.getBean("userService"));
//        OrderServiceImpl orderService = (OrderServiceImpl) applicationContext.getBeanByInterface(OrderService.class);
//        System.out.println(orderService.getUserService());
//        System.out.println(orderService.getBeamName());
        UserController userController = (UserController) applicationContext.getBean(UserController.class);
//        userController.print();
        userController.transfer();
    }

}
