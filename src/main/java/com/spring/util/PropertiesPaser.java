package com.spring.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesPaser {

    public static String jdbc_driver;
    public static String jdbc_url;
    public static String jdbc_username;
    public static String jdbc_password;

    static {
        try (
                InputStream inputStream = PropertiesPaser.class.getClassLoader().getResourceAsStream("db.properties")
        ) {
            if (inputStream == null) {
                throw new RuntimeException("no the properties file!");
            }
            Properties properties = new Properties();
            properties.load(inputStream);
            jdbc_driver = properties.getProperty("jdbc.driver");
            jdbc_url = properties.getProperty("jdbc.url");
            jdbc_username = properties.getProperty("jdbc.username");
            jdbc_password = properties.getProperty("jdbc.password");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }
}