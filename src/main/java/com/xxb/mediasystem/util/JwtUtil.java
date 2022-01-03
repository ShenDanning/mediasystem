package com.xxb.mediasystem.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import com.xxb.mediasystem.interceptor.UserContext;
import com.xxb.mediasystem.mapper.UserMapper;
import com.xxb.mediasystem.model.User;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;


@Component
public class JwtUtil {
    private final static byte[] ENCODE_KEY = "Vote-system".getBytes();
    private static JWTVerifier jwtVerifier;

    @Resource
    private UserMapper userMapper;

    public static String createJwt(String subject) {
        Date currentDate = new Date();
        // 过期时间5天
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 5);
        Algorithm algorithm = Algorithm.HMAC512(ENCODE_KEY);
        return "Bearer " + JWT.create()
                .withIssuedAt(currentDate)
                .withExpiresAt(calendar.getTime())
                .withSubject(subject)
                .sign(algorithm);
    }

    public static String parseJwt(String jwt) throws JWTVerificationException {
        Algorithm algorithm  = Algorithm.HMAC512(ENCODE_KEY);
        if (jwtVerifier == null){
            jwtVerifier = JWT.require(algorithm).build();
        }
        jwt = jwt.substring(7);
        jwtVerifier.verify(jwt);
        return JWT.decode(jwt).getSubject();
    }


    /**
     * @Description: 验证token的有效性
     * @Author: 0GGmr0
     * @Date: 2019-04-15
     */
    public int validateToken(String token) {
        // 除去 Bearer 开头
        token = token.substring(7);
        Algorithm algorithm = Algorithm.HMAC512(ENCODE_KEY);
        if (jwtVerifier == null) {
            // 创建校验器
            jwtVerifier = JWT.require(algorithm).build();
        }
        try {
            jwtVerifier.verify(token);
        } catch (SignatureVerificationException e) {
            // 签名内容失效
//            log.info("token签名内容失效，报错信息为{}", e.toString());
            return -3;
        } catch (TokenExpiredException e1) {
            // 时间失效
//            log.info("token时间过期，报错信息为{}", e1.toString());
            return -2;
        } catch (JWTDecodeException e2) {
//            log.info("token格式有误，报错信息为{}", e2.toString());
            return -1;
        }
        DecodedJWT decodedJWT = JWT.decode(token);
        String userId = decodedJWT.getSubject();
        User user = userMapper.selectByPrimaryKey(Integer.parseInt(userId));
        if (user == null) {
            return 1;
        }
        // 没有异常的话创建一个线程级的变量 专门存储user
        new UserContext(user);
        return 2;
    }

    private static String encode(String str){
        try {
            return URLEncoder.encode( str, "UTF-8" );
        } catch (UnsupportedEncodingException e) {
//            log.info("Issue while encoding" +e.getMessage());
//            throw new AllException(EmAllException.ENCODE_ERROR)
            return null;
        }

    }
}