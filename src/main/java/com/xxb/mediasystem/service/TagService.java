package com.xxb.mediasystem.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxb.mediasystem.mapper.PublicTagMapper;
import com.xxb.mediasystem.model.PublicTag;
import com.xxb.mediasystem.model.Type;
import com.xxb.mediasystem.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TagService {
    @Autowired
    public RedisTemplate redisTemplate;
    @Resource
    private PublicTagMapper publicTagMapper;

    public JSONObject getTagList(){
        String key = "tagList";
        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(key))) ;
        if(jsonObject == null){
            synchronized (this.getClass()){
                jsonObject = JSONObject.parseObject(JSON.toJSONString(redisTemplate.opsForValue().get(key))) ;
                if(jsonObject == null){
                    System.out.println("查询数据库");
                    List<PublicTag> tagList = publicTagMapper.getTagList();
                    JSONArray array= JSONArray.parseArray(JSON.toJSONString(tagList));
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("tagList",array);
                    jsonObject1.put("total",array.size());
                    redisTemplate.opsForValue().set(key,jsonObject1,1, TimeUnit.DAYS);
                    return  jsonObject1;
                }else {
                    System.out.println("查询缓存(同步代码块)");
                    return jsonObject;
                }
            }
        }else {
            System.out.println("查询缓存");
        }
      return jsonObject;

    }
//
//    @Cacheable(cacheNames="tag", key="tagList")
//    public JSONObject getTagList(){
//
//        JSONObject jsonObject  = new JSONObject();
//        System.out.println("查询数据库");
//        List<PublicTag> tagList = publicTagMapper.getTagList();
//        JSONArray array= JSONArray.parseArray(JSON.toJSONString(tagList));
//        jsonObject.put("tagList",array);
//        jsonObject.put("total",array.size());
//        return jsonObject;
//
//    }



}
