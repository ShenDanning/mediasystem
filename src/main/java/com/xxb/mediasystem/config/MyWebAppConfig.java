package com.xxb.mediasystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyWebAppConfig implements WebMvcConfigurer {

    //文件夹绝对路径
    @Value("${file.upload.abpath}")
    private String abpath;

    //文件夹url
    @Value("${file.upload.urlpath}")
    private String urlpath;

    public MyWebAppConfig() {
        System.out.println("注册进来了=====================");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(urlpath).addResourceLocations("file:"+abpath);
        WebMvcConfigurer.super.addResourceHandlers(registry);
    }
}
