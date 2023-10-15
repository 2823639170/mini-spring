package com.gjy.service.impl;

import com.gjy.service.UserService;
import com.spring.annotation.Component;

/**
 * @author 郭建勇
 * @date 2023/10/11
 **/
@Component
//@Scope(ScopeType.PROTOTYPE)
public class UserServiceImpl implements UserService {

    @Override
    public void print(){
        System.out.println("user service!");
    }

}
