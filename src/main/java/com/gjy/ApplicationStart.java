package com.gjy;

import com.gjy.config.AppConfig;
import com.gjy.service.OrderService;
import com.gjy.service.UserService;
import com.spring.GjySpringApplicationContext;

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
        OrderService orderService = (OrderService) applicationContext.getBean("orderService");
        System.out.println(orderService.getUserService());
        System.out.println(orderService.getBeamName());

    }

}
