package com.gjy.controller;

import com.gjy.service.UserService;
import com.spring.annotation.Autowired;
import com.spring.annotation.Component;
import com.spring.annotation.Controller;


/**
 * @author 郭建勇
 * @date 2023/10/14
 **/
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    public void print(){
        userService.print();
    }
    public void transfer(){
        userService.transfer(1 , 2 , 10);
    }


}
