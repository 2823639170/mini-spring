package com.springmvc.servlet;

import com.gjy.config.AppConfig;
import com.spring.GjySpringApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author 郭建勇
 * @date 2023/10/16
 **/
public class DispatcherServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
        // 启动spring
        GjySpringApplicationContext context = new GjySpringApplicationContext(AppConfig.class);
        Map<Class, Object> controllerMap = context.getControllerMap();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
