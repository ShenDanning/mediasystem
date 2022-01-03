package com.xxb.mediasystem.interceptor;

import cn.yueshutong.springbootstartercurrentlimiting.annotation.CurrentLimiter;
import cn.yueshutong.springbootstartercurrentlimiting.handler.CurrentAspectHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxb.mediasystem.util.Result;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class MyCurrentLimitHandler implements CurrentAspectHandler {
    @Override
    public Result around(ProceedingJoinPoint pjp, CurrentLimiter rateLimiter)  {
      return Result.build(500,"系统繁忙");
    }
}
