package com.spring.transactional;

import com.spring.util.PropertiesPaser;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author 郭建勇
 * @date 2023/10/15
 **/
public class TransactionalManager {

    private static ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<Connection>();

    static {
        connectionThreadLocal.set(getConnection());
    }

    private static Connection getConnection() {
        try {
            Class.forName(PropertiesPaser.jdbc_driver);
            Connection conn = DriverManager.getConnection(PropertiesPaser.jdbc_url, PropertiesPaser.jdbc_username, PropertiesPaser.jdbc_password);
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return null;
    }

    public static Connection getThreadLocalConnect() {
        return connectionThreadLocal.get();
    }


}
