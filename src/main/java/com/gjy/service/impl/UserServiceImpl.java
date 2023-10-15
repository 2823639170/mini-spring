package com.gjy.service.impl;

import com.gjy.dao.UserDao;
import com.gjy.service.UserService;
import com.spring.annotation.Autowired;
import com.spring.annotation.Component;

/**
 * @author 郭建勇
 * @date 2023/10/11
 **/
@Component
//@Scope(ScopeType.PROTOTYPE)
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    public void print(){
        System.out.println("user service!");
        String username = userDao.getUsername();
        System.out.println(username);
    }

}
