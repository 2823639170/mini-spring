package com.gjy.service.impl;

import com.gjy.service.BookService;
import com.gjy.service.UserService;
import com.spring.annotation.Autowired;
import com.spring.annotation.Component;

/**
 * @author 郭建勇
 * @date 2023/10/14
 **/
@Component
public class BookServiceImpl implements BookService {

    @Autowired
    private UserService userService;





}
