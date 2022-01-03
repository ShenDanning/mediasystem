package com.xxb.mediasystem.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xxb.mediasystem.mapper.TypeMapper;
import com.xxb.mediasystem.mapper.UserMapper;
import com.xxb.mediasystem.mapper.VideoMapper;
import com.xxb.mediasystem.model.PublicTag;
import com.xxb.mediasystem.model.Type;
import com.xxb.mediasystem.model.User;
import com.xxb.mediasystem.model.Video;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class TypeService {
    @Resource
    private TypeMapper typeMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private VideoMapper videoMapper;
    @Autowired
    private VideoService videoService;

    /**
    *@描述 根据用户的ID返回用户创建的所有分类
    *@创建人 dyj
    *@创建时间 2021/12/2
    **/
    public JSONObject getTypeList(HttpServletRequest request){
        String token = request.getHeader("Authorization");
      //  User user = userMapper.findUserByToken(token);
        String userId = request.getHeader("userId");
        System.out.println(userId);
        List<Type> typeList = typeMapper.getTypeList(Integer.valueOf(userId));

        JSONArray jsonArray = new JSONArray();
        for(Type type:typeList){
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("id",type.getId());
            jsonObject1.put("type",type.getType());
            jsonObject1.put("author",userMapper.selectByPrimaryKey(type.getAuthor()).getName());
            jsonArray.add(jsonObject1);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("typeList",jsonArray);
        jsonObject.put("total",jsonArray.size());
        return  jsonObject;
    }


    /**
    *@描述 添加分类
    *@创建人 dyj
    *@创建时间 2021/12/2
    **/
    public int addType(Type type){
        return typeMapper.addType(type);
    }

    public int editType(Type type){
        return typeMapper.editType(type);
    }

    public int deleteType(Type type){
        List<Video> videoList = videoMapper.getVideoByType(Integer.valueOf(type.getId().toString()));
        if(videoList.size()!=0){
            for(Video video:videoList){//分类删除，对视频进行逻辑删除
               videoService.deleteVideoLoji(video.getId());
            }
            return typeMapper.deleteType(type);
        }else{
            return typeMapper.deleteType(type);
        }
    }
}
