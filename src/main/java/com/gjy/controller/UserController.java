package com.gjy.controller;

import com.gjy.service.UserService;
import com.spring.annotation.Autowired;
import com.spring.annotation.Component;


/**
 * @author 郭建勇
 * @date 2023/10/14
 **/
@Component
public class UserController {

    @Autowired
    private UserService userService;

    public void print(){
        userService.print();
    }


}
