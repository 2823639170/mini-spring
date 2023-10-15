package com.gjy.dao.impl;

import com.gjy.dao.UserDao;
import com.spring.annotation.Component;

/**
 * @author EVA
 */
@Component
public class UserDaoImpl implements UserDao {

    @Override
    public String getUsername() {
        System.out.println("DAO服务");
        return "---GJY---";
    }
}
