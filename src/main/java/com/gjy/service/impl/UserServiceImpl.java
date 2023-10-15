package com.gjy.service.impl;

import com.gjy.dao.UserDao;
import com.gjy.service.UserService;
import com.spring.annotation.Autowired;
import com.spring.annotation.Component;
import com.spring.annotation.Transactional;

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
    public void print() {
        System.out.println("user service!");
        String username = userDao.getUsername();
        System.out.println(username);
    }

    @Override
    @Transactional
    public void transfer(int fromId, int toId, int money) {
        userDao.updateMoney(fromId, -money);
        System.out.println(fromId +"用户已经扣钱");
//        int num = 10/0;
        userDao.updateMoney(toId, money);
        System.out.println(toId +"用户已经加钱");
        System.out.println("转账完成！");
    }

}
