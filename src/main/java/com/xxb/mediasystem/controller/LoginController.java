package com.xxb.mediasystem.controller;
import com.xxb.mediasystem.model.SsoQuery;
import com.xxb.mediasystem.service.UserService;
import com.xxb.mediasystem.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @program: vote
 * @description: 登录控制器
 * @author: ggmr
 * @create: 2018-06-17 02:06
 */
@RestController
public class LoginController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    public Result loginSSO(@RequestBody SsoQuery ssoQuery, HttpServletRequest request) {
        return userService.loginSSO(request,ssoQuery);
    }
    /**
    *@描述 获取用户的权限
    *@创建人 dyj
    *@创建时间 2021/12/27
    **/
    @GetMapping("/getIdentity")
    public Result getIdentity(HttpServletRequest request){
        return userService.getIdentity(request);
    }


}
