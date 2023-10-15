package com.gjy.dao.impl;

import com.gjy.dao.UserDao;
import com.spring.annotation.Component;
import com.spring.transactional.TransactionalManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.locks.Condition;

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

    @Override
    public int updateMoney(int id, int money) {
        Connection connect = TransactionalManager.getThreadLocalConnect();
        try {
            PreparedStatement preparedStatement = connect.prepareStatement("update t_user set money = money + ? where `id` = ?");
            preparedStatement.setObject(1 , money);
            preparedStatement.setObject(2, id);
            int i = preparedStatement.executeUpdate();
            return i;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            // 正常使用完是要关闭的，但是这里要处理事务所以不关闭，最后由spring统一关闭
        }
        return 0;
    }
}
