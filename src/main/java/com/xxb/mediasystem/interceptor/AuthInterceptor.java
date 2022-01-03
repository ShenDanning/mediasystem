package com.xxb.mediasystem.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.xxb.mediasystem.util.JwtUtil;
import com.xxb.mediasystem.util.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * @description: 拦截器
 * @author: 0GGmr0
 * @create: 2019-12-01 21:38
 */


public class AuthInterceptor implements HandlerInterceptor {

    private static final String LOGIN_URL = "/login";
    private static final String TOKEN_NAME = "Authorization";
    private static final String OPTION_METHOD = "OPTIONS";
    @Resource
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws IOException {
        // 登陆接口不做拦截及解决跨域问题
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type,Content-Length, Authorization,userId,Accept,X-Requested-With");
        response.setHeader("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS");

        // TODO 暂时只做了一个很简单的TOKEN验证，只判断有无，不判断是否过期或者修改过某些内容，也没有吊销token的操作
        String token = request.getHeader(TOKEN_NAME);
        System.out.println(request.getRequestURL());
        //System.out.println("token"+token);
        if(request.getMethod().equals("OPTIONS")||request.getRequestURI().equals("/v1/login")||request.getRequestURI().contains("/v1/getVideoByCollectionId")){
            return true;
        }
        if (token == null) {
            returnErrorMessage(response, "请登录后再访问页面", 401);
            return false;
        } else {
            // 验证tokne是否是临时token
            switch (jwtUtil.validateToken(token)) {
                case 2:
//                        int index = requestUri.indexOf("/", 5);
//                        String targetIdentity = requestUri.toLowerCase().substring(5, index > 0 ? index : requestUri.length() - 1);
//                        if (!"user|file".contains(targetIdentity) && !UserContext.getCurrentUser().getType().contains(targetIdentity)) {
//                            returnErrorMessage(response, "无权限访问", 403);
//                            return false;
//                        }
                    return true;
                case -3:
                    returnErrorMessage(response, "token签名内容失效", 401);
                    return false;
                case -2:
                    returnErrorMessage(response, "token已超时", 401);
                    return false;
                case -1:
                    returnErrorMessage(response, "虚假token", 401);
                    return false;
                default:
                    return true;
            }
        }
    }

    private void returnErrorMessage(HttpServletResponse response, String msg, int code) throws IOException {
        Result result = Result.build(code,msg);
        response.setContentType("application.yml/json;charset=utf-8");
        response.setStatus(403);
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        String jsonOfRST = mapper.writeValueAsString(result);
        out.print(jsonOfRST);
        out.flush();
    }
}

