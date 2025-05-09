package com.bearc.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class UserInfoController {
    /*
    * 当前登录的用户对象
    * */
    @GetMapping("/user/info")
    public Principal userInfo(Principal user) {
        //user等价于  SecurityContextHolder.getContext().getAuthentication();
        return user;
    }
}
