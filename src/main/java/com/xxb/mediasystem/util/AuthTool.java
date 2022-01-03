package com.xxb.mediasystem.util;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWTVerifier;

import com.xxb.mediasystem.mapper.UserMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @program: vote
 * @description: 学校接口
 * @author: ggmr
 * @create: 2018-06-17 02:26
 */

@Component
public class AuthTool {


    private final static byte[] ENCODE_KEY = "meeting".getBytes();
    private final static String TOKEN_HEADER = "Bearer ";

    private static JWTVerifier jwtVerifier;

    @Resource
    private UserMapper userMapper;
    @Value("${sso.clientId}")
    private String clientId;

    @Value("${sso.secret}")
    private String clientSecret;
    /**
     * @Description: 验证token的有效性
     * @Author: 0GGmr0
     * @Date: 2019-04-15
     */

//    @Slf4j
//    @Component
//    public int validateToken(String token) throws UnsupportedEncodingException {
//        // 除去 Bearer 开头
//        token = token.substring(7);
//        Algorithm algorithm = Algorithm.HMAC256(ENCODE_KEY);
//        if (jwtVerifier == null) {
//            // 创建校验器
//            jwtVerifier = JWT.require(algorithm).build();
//        }
//        try {
//            jwtVerifier.verify(token);
//        } catch (SignatureVerificationException e) {
//            // 签名内容失效
//            log.info("token签名内容失效，报错信息为{}", e.toString());
//            return SIGNATURE_VERIFICATION_EXCEPTION;
//        } catch (TokenExpiredException e1) {
//            // 时间失效
//            log.info("token时间过期，报错信息为{}", e1.toString());
//            return TOKEN_EXPIRED_EXCEPTION;
//        } catch (JWTDecodeException e2) {
//            log.info("token格式有误，报错信息为{}", e2.toString());
//            return FAKE_TOKEN;
//        }
//        DecodedJWT decodedJWT = JWT.decode(token);
//        String userId = decodedJWT.getSubject();
//        User user = userMapper.selectByPrimaryKey(Integer.parseInt(userId));
//        if (user == null) {
//            return FAKE_TOKEN;
//        }
//        // 创建一个线程级的变量 专门存储user
//        new UserContext(user);
//        String name = URLDecoder.decode(
//                decodedJWT.getClaim("name").asString(), "UTF-8");
//        if(name.equals(user.getName())) {
//            // 如果nickname正常，说明是正确的token
//            return NORMAL_TOKEN;
//        } else {
//            // 这是一个伪造的token
//            return FAKE_TOKEN;
//        }
//    }

    public JSONObject SSOGetToken(String code, String redirectUrl) {
        try {
            //api地址
            String url = "https://oauth.shu.edu.cn/oauth/token?grant_type=authorization_code&" +
                    "code=" + code + "&client_id=" + clientId + "&client_secret=" + clientSecret
                    + "&redirect_uri=" + redirectUrl;

            CloseableHttpClient httpClient = HttpClients.createDefault();
            // 请求
            HttpGet httpGet = new HttpGet(url);

            CloseableHttpResponse response = httpClient.execute(httpGet);

            String result = EntityUtils.toString(response.getEntity());

            response.close();
            httpClient.close();
            //使用fastJson解析网页
            return JSONObject.parseObject(result);
        } catch (ClientProtocolException e) {
//          // TODO 不清楚前后端异常处理格式，这里就不作处理了

            return null;
        } catch (IOException e) {

            return null;
        }
    }

    public String getCollegeName(String token) {
        JSONObject userInfo = SSOGetInfo(token);

        return userInfo.getString("department");
    }

    public JSONObject SSOGetInfo(String token) {
        try {
            //api地址
            String url = "https://oauth.shu.edu.cn/rest/user/getLoggedInUser";
            CloseableHttpClient httpClient = HttpClients.createDefault();
            // 请求
            HttpPost httpPost = new HttpPost(url);

            //Post内容
            List<NameValuePair> nameValuePairList = new ArrayList<>();
            nameValuePairList.add(new BasicNameValuePair("access_token", token));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList, "UTF-8"));

            CloseableHttpResponse response = null;
            response = httpClient.execute(httpPost);
            String result = EntityUtils.toString(response.getEntity());

            response.close();
            httpClient.close();
            //使用fastJson解析网页
            return JSONObject.parseObject(result);
        } catch (ClientProtocolException e) {

            return null;
        } catch (IOException e) {

            return null;
        }
    }






}