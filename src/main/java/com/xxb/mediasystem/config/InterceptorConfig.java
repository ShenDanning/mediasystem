package com.xxb.mediasystem.config;



import com.xxb.mediasystem.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {



    @Bean
    public AuthInterceptor authInterceptor() {
        return new AuthInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 放行路径
        List<String> patterns = new ArrayList();
        patterns.add("/video/**");
        patterns.add("/login");
        patterns.add("/public/**");
        patterns.add("/collections/getPublished");
        patterns.add("/getVideoByCollectionId?");
        registry.addInterceptor(authInterceptor()).addPathPatterns("/**")
                .excludePathPatterns(patterns);
    }
}
