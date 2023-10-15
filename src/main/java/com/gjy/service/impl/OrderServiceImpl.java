package com.gjy.service.impl;

import com.gjy.service.OrderService;
import com.gjy.service.UserService;
import com.spring.Interface.BeanNameAware;
import com.spring.Interface.InitializingBean;
import com.spring.annotation.Autowired;
import com.spring.annotation.Component;
import lombok.Data;

/**
 * @author 郭建勇
 * @date 2023/10/11
 **/
@Component
@Data
public class OrderServiceImpl implements OrderService , BeanNameAware , InitializingBean {
    @Autowired
    private UserService userService;

    private String beamName;


    @Override
    public void setBeamName(String beamName) {
        this.beamName = beamName;
    }

    @Override
    public void afterPropertySet() {
        System.out.println("调用afterPropertySet方法！");
    }
}
