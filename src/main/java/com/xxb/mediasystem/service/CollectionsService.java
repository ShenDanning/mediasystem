package com.xxb.mediasystem.service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xxb.mediasystem.mapper.CollectionsMapper;
import com.xxb.mediasystem.mapper.UserMapper;
import com.xxb.mediasystem.mapper.VideoMapper;
import com.xxb.mediasystem.model.Collections;
import com.xxb.mediasystem.model.Type;
import com.xxb.mediasystem.model.Video;
import com.xxb.mediasystem.sensitiveWords.SensitivewordFilter;
import com.xxb.mediasystem.util.FileUtil;
import com.xxb.mediasystem.util.PageHelperUtil;
import com.xxb.mediasystem.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CollectionsService {
    @Autowired
    private PageHelperUtil pageHelperUtil;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private FdfsService fdfsService;
    @Resource
    private CollectionsMapper collectionsMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private VideoMapper videoMapper;
    @Value("${file.upload.abpath}")



    private String abpath;
    //@CachePut(cacheNames="user", key="#user.id")
    public int addCollection(Collections collections){
        redisTemplate.delete("collections");
        getPublishedCollectionsdb();
        return collectionsMapper.addCollection(collections);
    }


    public JSONObject getCollections(Integer curPage, Integer pageSize,HttpServletRequest request){
        String userId = request.getHeader("userId");
        long total;
        Page page = PageHelper.startPage(curPage,pageSize);
        List<Collections> collectionsList = collectionsMapper.getCollections(Integer.valueOf(userId));
        PageInfo<Collections> pageInfo = new PageInfo<> (collectionsList);
        total = pageInfo.getTotal();
        JSONArray jsonArray = new JSONArray();
        for(Collections collections:collectionsList){
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("id",collections.getId());
            SensitivewordFilter filter = new SensitivewordFilter();
            jsonObject1.put("name",filter.replaceSensitiveWord(collections.getName(),1,"*"));
            jsonObject1.put("picture",collections.getPicture());
            jsonObject1.put("videos",videoMapper.getVideoByCollectionId(collections.getId()).size());
            jsonObject1.put("description",filter.replaceSensitiveWord(collections.getDescription(),1,"*"));
            jsonObject1.put("createdTime",collections.getCreatedTime());
            jsonObject1.put("publish",collections.getPublish());
            jsonObject1.put("author",userMapper.selectByPrimaryKey(collections.getAuthor()).getName());
            jsonArray.add(jsonObject1);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("collectionsList",jsonArray);
        jsonObject.put("total",total);
        return  jsonObject;
    }

    public int editCollections(Collections collections,Integer isImgEdit){
        Collections oldCollection = collectionsMapper.getCollectionById(collections.getId());
        String oldPicture = oldCollection.getPicture();

        if(collectionsMapper.editCollections(collections)==1){//删除旧照片
            if(!oldPicture.equals("")&&isImgEdit==1){
                //本地测试
              //  FileUtil.delete(abpath+oldPicture.split("video/")[1]);
                //部署
                FileUtil.delete(abpath+"assets/"+oldPicture.split("106/")[1]);
            }
            redisTemplate.delete("collections");
            getPublishedCollectionsdb();

            return 1;

        }else{
            redisTemplate.delete("collections");
            getPublishedCollectionsdb();
            return 0;
        }

    }

    public int editCollectionsTofdfs(Collections collections,Integer isImgEdit){
        Collections oldCollection = collectionsMapper.getCollectionById(collections.getId());
        String oldPicture = oldCollection.getPicture();
        if(collectionsMapper.editCollections(collections)==1){//删除旧照片
            if(!oldPicture.equals("")&&isImgEdit==1){
                fdfsService.deleteFile(oldPicture.split("106/")[1]);
            }
            return 1;

        }else{
            return 0;
        }
    }

    public int deleteCollection(Long collectionId){

        List<Video> videoList = videoMapper.getVideoByCollectionId(collectionId);
        for(Video video:videoList){//把该专栏下的视频删除
            videoMapper.deleteVideoLoji(video.getId());
        }
        redisTemplate.delete("collections");
        getPublishedCollectionsdb();
        return collectionsMapper.deleteCollection(collectionId);
    }

   // @Cacheable(cacheNames="collections", key="#curPage+'/'+#pageSize")
    public JSONObject getPublishedCollections(Integer curPage,Integer pageSize){
       // System.out.printlnz("查询数据库");
//        long total;
//        Page page = PageHelper.startPage(curPage,pageSize);
//        List<Collections> collectionsList = collectionsMapper.getPublishedCollections();
//        PageInfo<Collections> pageInfo = new PageInfo<> (collectionsList);
//        total = pageInfo.getTotal();
//        JSONArray jsonArray = new JSONArray();
//        for(Collections collections:collectionsList){
//            JSONObject jsonObject1 = new JSONObject();
//            jsonObject1.put("id",collections.getId());
//            jsonObject1.put("name",collections.getName());
//            jsonObject1.put("picture",collections.getPicture());
//            jsonObject1.put("videos",collections.getVideos()==null?0:collections.getVideos());
//            jsonObject1.put("description",collections.getDescription());
//            jsonObject1.put("createdTime",collections.getCreatedTime());
//            jsonObject1.put("author",userMapper.selectByPrimaryKey(collections.getAuthor()).getName());
//            jsonArray.add(jsonObject1);
//        }
        String key = "collections";
        List collections = pageHelperUtil.getPageHelperList(key,curPage,pageSize);
        System.out.println(collections);
        if(redisTemplate.opsForList().size(key)==0){
            synchronized (this.getClass()){
                collections = pageHelperUtil.getPageHelperList(key,curPage,pageSize);
                if(redisTemplate.opsForList().size(key)==0){
                    getPublishedCollectionsdb();
                    collections = pageHelperUtil.getPageHelperList(key,curPage,pageSize);
                }else {
                    System.out.println("查询缓存(同步代码块)");
                }
            }
        }else{
            System.out.println("查询缓存");
        }
        JSONArray jsonArray= JSONArray.parseArray(JSON.toJSONString(collections));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("collectionsList",jsonArray);
        jsonObject.put("total",jsonArray.size());
        return  jsonObject;
    }
    public void getPublishedCollectionsdb(){
        List<Collections> collectionsList = collectionsMapper.getPublishedCollections();

        System.out.println("查询数据库");
        JSONArray jsonArray = new JSONArray();
        for(Collections collections:collectionsList){
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("id",collections.getId());
//            jsonObject1.put("name",collections.getName());
            SensitivewordFilter filter = new SensitivewordFilter();
            jsonObject1.put("name",filter.replaceSensitiveWord(collections.getName(),1,"*"));
            jsonObject1.put("picture",collections.getPicture());
            jsonObject1.put("videos",collections.getVideos()==null?0:collections.getVideos());
            jsonObject1.put("description",filter.replaceSensitiveWord(collections.getDescription(),1,"*"));
            jsonObject1.put("createdTime",collections.getCreatedTime());
            jsonObject1.put("author",userMapper.selectByPrimaryKey(collections.getAuthor()).getName());
            jsonArray.add(jsonObject1);
        }
        if(jsonArray!=null&&jsonArray.size()!=0){
            redisTemplate.opsForList().rightPushAll("collections", jsonArray);
        }


    }

    public JSONObject getPublishedCollectionsByName(String name,Integer curPage,Integer pageSize){
        long total;
        Page page = PageHelper.startPage(curPage,pageSize);
        List<Collections> collectionsList = collectionsMapper.getPublishedCollectionsByName(name);
        PageInfo<Collections> pageInfo = new PageInfo<> (collectionsList);
        total = pageInfo.getTotal();
        JSONArray jsonArray = new JSONArray();
        for(Collections collections:collectionsList){
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("id",collections.getId());
            SensitivewordFilter filter = new SensitivewordFilter();
            jsonObject1.put("name",filter.replaceSensitiveWord(collections.getName(),1,"*"));
            jsonObject1.put("picture",collections.getPicture());
            jsonObject1.put("videos",collections.getVideos()==null?0:collections.getVideos());
            jsonObject1.put("description",filter.replaceSensitiveWord(collections.getDescription(),1,"*"));
            jsonObject1.put("createdTime",collections.getCreatedTime());
            jsonObject1.put("author",userMapper.selectByPrimaryKey(collections.getAuthor()).getName());
            jsonArray.add(jsonObject1);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("collectionsList",jsonArray);
        jsonObject.put("total",total);
        return  jsonObject;
    }


    public int setPublish(Collections collections){
        redisTemplate.delete("collections");
        getPublishedCollectionsdb();
        return collectionsMapper.editCollections(collections);
    }

    public Result setUp(Long videoId){
        Video video = videoMapper.getVideoById(videoId);
        Long collectionId = video.getCollectionId();
        int curSortId = video.getSortId();
        System.out.println(curSortId);
        if(curSortId==1) {
            return Result.build(400, "当前视频在第一个,不能再上移");
        }else{
            video.setSortId(curSortId-1);
            Video preVideo = videoMapper.getAllVideoByCollectionId(collectionId).get(curSortId-2);
            preVideo.setSortId(curSortId);
            videoMapper.editVideoInfo(video);
            videoMapper.editVideoInfo(preVideo);
            return Result.build(200,"上移成功");
        }
    }

    public Result setDown(Long videoId){
        Video video = videoMapper.getVideoById(videoId);
        Long collectionId = video.getCollectionId();
        int num = videoMapper.getAllVideoByCollectionId(collectionId).size();
        int curSortId = video.getSortId();
        if(curSortId==num) {
            return Result.build(400, "当前视频在最后一个,不能再下移");
        }else{
            video.setSortId(curSortId+1);
            Video preVideo = videoMapper.getAllVideoByCollectionId(collectionId).get(curSortId);
            preVideo.setSortId(curSortId);
            videoMapper.editVideoInfo(video);
            videoMapper.editVideoInfo(preVideo);
            return Result.build(200,"下移成功");
        }
    }


    //获取待审核的合集
    public JSONObject getAuditCollections(Integer curPage, Integer pageSize) {
        long total;
        Page page = PageHelper.startPage(curPage,pageSize);
        List<Collections> collectionsList = collectionsMapper.getAuditCollections();
        PageInfo<Collections> pageInfo = new PageInfo<> (collectionsList);
        total = pageInfo.getTotal();
        JSONArray jsonArray = new JSONArray();
        for(Collections collections:collectionsList){
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("id",collections.getId());
            SensitivewordFilter filter = new SensitivewordFilter();
            //待审核的视频不进行敏感词过滤
            jsonObject1.put("name",collections.getName());
            jsonObject1.put("picture",collections.getPicture());
            jsonObject1.put("videos",collections.getVideos()==null?0:collections.getVideos());
            jsonObject1.put("description",collections.getDescription());
            jsonObject1.put("createdTime",collections.getCreatedTime());
            jsonObject1.put("author",userMapper.selectByPrimaryKey(collections.getAuthor()).getName());
            jsonArray.add(jsonObject1);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("collectionsList",jsonArray);
        jsonObject.put("total",total);
        return  jsonObject;
    }
}
