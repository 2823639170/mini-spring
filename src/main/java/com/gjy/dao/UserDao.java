package com.gjy.dao;

import java.lang.reflect.GenericArrayType;

/**
 * @author EVA
 */
public interface UserDao {

    String getUsername();

    int updateMoney(int id, int money);
}
