package com.xxb.mediasystem.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xxb.mediasystem.mapper.*;
import com.xxb.mediasystem.model.Collections;
import com.xxb.mediasystem.model.PublicTag;
import com.xxb.mediasystem.model.User;
import com.xxb.mediasystem.model.Video;
import com.xxb.mediasystem.sensitiveWords.SensitivewordFilter;
import com.xxb.mediasystem.util.FileUtil;
import com.xxb.mediasystem.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Date;
import java.util.List;

@Service
public class VideoService {
    @Resource
    private VideoMapper videoMapper;
    @Resource
    private TypeMapper typeMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private PublicTagMapper tagMapper;
    @Resource
    private CollectionsMapper collectionsMapper;
    @Value("${file.upload.abpath}")
    private String abpath;
    //上传图片Url
    @Value("${file.upload.mdImageDir}")
    private String mdImageDir;
    //上传视频Url
    @Value("${file.upload.mdImageDir}")
    private String mdVideoDir;

    //端口号
    @Value("${server.port}")
    private String port;

    @Value("${file.upload.serverUrl}")
    private String imgUrlDir;


    @Autowired
    private FdfsService fdfsService;


    public Result uploadVideo(MultipartFile file, MultipartFile picture, String description, String title, Integer typeId, HttpServletRequest request){
        //绝对路径
        String AbPath = abpath;
        //上传视频
        File f = FileUtil.upload(file, AbPath);
        //将视频信息存储至数据库
        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setType(typeId);
        video.setCollectionId(Long.valueOf(0));
        //String token = request.getHeader("Authorization");
        String userId = request.getHeader("userId");
        //video.setAuthor(userMapper.findUserByToken(token).getId());
        video.setAuthor(Integer.valueOf(userId));
        String imgAbPath = abpath + "/assets/";
        //返回对应的File类型f
        File img = FileUtil.upload(picture, imgAbPath);
//        try {//封面上传至fastdfs
//            String picturePath=fdfsService.upToFdfs(picture);
//            video.setPicture(imgUrlDir+'/'+picturePath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        video.setFileName(f.getName());
        Date now = new Date();
        video.setUploadTime(now);
        video.setPath(AbPath);
        video.setUrl(imgUrlDir+"/"+f.getName());
        video.setPicture(imgUrlDir+'/'+img.getName());
        if(addVideo(video)==1){
            return Result.build(200, "保存成功");
        }else {
            return Result.build(400,"上传失败");
        }
    }


    /**
   *@描述 数据库中添加一个视频
   *@参数 
   *@返回值
   *@创建人 dyj
   *@创建时间 2021/11/22
   **/


    public int addVideo(Video video){
       return  videoMapper.addVideo(video);
    }
    public JSONObject getPersonalVideo(Integer curPage, Integer pageSize, HttpServletRequest request){//加用户id
        String userId = request.getHeader("userId");
        User user = userMapper.selectByPrimaryKey(Integer.valueOf(userId));
        //User user = userMapper.findUserByToken(token);
        long total;
        Page page = PageHelper.startPage(curPage,pageSize);
        List<Video> videoList = videoMapper.getPersonalVideo(Integer.valueOf(userId));
        PageInfo<Video> pageInfo = new PageInfo<>(videoList);
        total = pageInfo.getTotal();
        JSONArray jsonArray = new JSONArray();
        for(Video video:videoList){
            JSONObject jsonObject1 = new JSONObject();
            SensitivewordFilter filter = new SensitivewordFilter();
            jsonObject1.put("id",video.getId());
            jsonObject1.put("title",filter.replaceSensitiveWord(video.getTitle(),1,"*"));
            jsonObject1.put("description",filter.replaceSensitiveWord(video.getDescription(),1,"*"));
            jsonObject1.put("picture",video.getPicture());
            jsonObject1.put("url",video.getUrl());
            jsonObject1.put("fileName",video.getFileName());
            jsonObject1.put("author",user.getName());
            jsonObject1.put("typeId",video.getType());
            jsonObject1.put("uploadTime",video.getUploadTime());
            jsonObject1.put("views",video.getViews());
            jsonObject1.put("publish",video.getPublish());
            jsonObject1.put("tag",tagMapper.getTagById(video.getTag()) );
            jsonObject1.put("type",typeMapper.getTypeById(video.getType()));
            jsonArray.add(jsonObject1);

        }

        JSONObject  jsonObject = new JSONObject();

        jsonObject.put("videoList",jsonArray);
        jsonObject.put("total",total);
        return jsonObject;
    }

    public JSONObject getAllVideo(Integer curPage,Integer pageSize){
        long total;
        Page page = PageHelper.startPage(curPage,pageSize);
        List<Video> videoList = videoMapper.getAllVideo();
        PageInfo<Video> pageInfo = new PageInfo<>(videoList);
        total = pageInfo.getTotal();
        JSONArray jsonArray = new JSONArray();
        for(Video video:videoList){
            JSONObject jsonObject1 = new JSONObject();
            SensitivewordFilter filter = new SensitivewordFilter();
            jsonObject1.put("id",video.getId());
            jsonObject1.put("title",filter.replaceSensitiveWord(video.getTitle(),1,"*"));
            jsonObject1.put("description",filter.replaceSensitiveWord(video.getDescription(),1,"*"));
            jsonObject1.put("picture",video.getPicture());
            jsonObject1.put("url",video.getUrl());
            jsonObject1.put("fileName",video.getFileName());
            jsonObject1.put("author",userMapper.selectByPrimaryKey(video.getAuthor()));
            jsonObject1.put("typeId",video.getType());
            jsonObject1.put("uploadTime",video.getUploadTime());
            jsonObject1.put("publish",video.getPublish());
            jsonObject1.put("views",video.getViews());
            jsonObject1.put("tag",tagMapper.getTagById(video.getTag()) );
            jsonObject1.put("type",typeMapper.getTypeById(video.getType()));
            jsonArray.add(jsonObject1);

        }


        JSONObject  jsonObject = new JSONObject();
        jsonObject.put("videoList",jsonArray);
        jsonObject.put("total",total);
        return jsonObject;
    }

    public int editVideoInfo(Video video,Integer flag){
        Video video1 = videoMapper.getVideoById(video.getId());
        String oldPicture = video1.getPicture();
        if(videoMapper.editVideoInfo(video)==1){
            if(!oldPicture.equals("")&&flag==1){//如果修改图片，需要将原先的图片删除
                System.out.println("删除");
              //  fdfsService.deleteFile(oldPicture.split("8888/")[1]);//fastdfs删除
               // System.out.println(video1.getPath()+"assets/"+video.getPicture().split("assets/")[1]);
                FileUtil.delete(video1.getPath()+"assets/"+oldPicture.split("106/")[1]);
            }
            return 1;
        }else{
            return 0;
        }
    }
    public int editVideoInfofdfs(Video video,Integer flag){
        Video video1 = videoMapper.getVideoById(video.getId());
        String oldPicture = video1.getPicture();
        if(videoMapper.editVideoInfo(video)==1){
            if(!oldPicture.equals("")&&flag==1){//如果修改图片，需要将原先的图片删除
                System.out.println("删除");
                fdfsService.deleteFile(oldPicture.split("106/")[1]);//fastdfs删除
            }
            return 1;
        }else{
            return 0;
        }
    }

    public int deleteVideo(Long videoId){
        Video video = videoMapper.getVideoById(videoId);
        String oldPicture = video.getPicture();
        //删除视频
        if(FileUtil.delete(video.getPath()+video.getFileName())){
            videoMapper.deleteVideo(videoId);
            if(!oldPicture.equals("")){//删除视频将封面图片删除
                System.out.println(video.getPath()+"assets/"+video.getPicture().split("106/")[1]);
             //   fdfsService.deleteFile(oldPicture.split("8888/")[1]);
                FileUtil.delete(video.getPath()+"assets/"+oldPicture.split("106/")[1]);
            }
            return 1;
        }else{
            return 0;
        }
    }

    public int deleteVideoLoji(Long videoId){
        Video video = videoMapper.getVideoById(videoId);
        Long collectionId = video.getCollectionId();

        if(collectionId!=0){//
            int deleteSortId = video.getSortId();
            List<Video> videoList = videoMapper.getAllVideoByCollectionId(collectionId);
            for(Video video1:videoList){
                int curSortId = video1.getSortId();
                if(curSortId>deleteSortId){
                    video1.setSortId(curSortId-1);
                    videoMapper.editVideoInfo(video1);
                }

            }

            Collections collections = collectionsMapper.getCollectionById(collectionId);
            int num = videoMapper.getVideoByCollectionId(collectionId).size();
            collections.setId(collectionId);
            collections.setVideos(num);

            collectionsMapper.editCollections(collections);
        }
        return videoMapper.deleteVideoLoji(videoId);
    }

    public JSONObject getVideoByTitle(String title,Integer curPage,Integer pageSize,HttpServletRequest request){
        String userId = request.getHeader("userId");
        User user = userMapper.selectByPrimaryKey(Integer.valueOf(userId));
        long total;
        Page page = PageHelper.startPage(curPage,pageSize);
        List<Video> videoList = videoMapper.getVideoByTitle(title);
        PageInfo<Video> pageInfo = new PageInfo<>(videoList);
        total = pageInfo.getTotal();
        JSONArray jsonArray = new JSONArray();
        for(Video video:videoList){
            JSONObject jsonObject1 = new JSONObject();
            SensitivewordFilter filter = new SensitivewordFilter();
            jsonObject1.put("id",video.getId());
            jsonObject1.put("title",filter.replaceSensitiveWord(video.getTitle(),1,"*"));
            jsonObject1.put("description",filter.replaceSensitiveWord(video.getDescription(),1,"*"));
            jsonObject1.put("picture",video.getPicture());
            jsonObject1.put("url",video.getUrl());
            jsonObject1.put("fileName",video.getFileName());
            jsonObject1.put("author",user.getName());
            jsonObject1.put("typeId",video.getType());
            jsonObject1.put("uploadTime",video.getUploadTime());
            jsonObject1.put("type",typeMapper.getTypeById(video.getType()));
            jsonObject1.put("tag",tagMapper.getTagById(video.getTag()) );
            jsonObject1.put("publish",video.getPublish());
            jsonObject1.put("views",video.getViews());
            jsonArray.add(jsonObject1);
        }
        JSONObject  jsonObject = new JSONObject();
        jsonObject.put("videoList",jsonArray);
        jsonObject.put("total",total);
        return jsonObject;
    }

    public JSONObject getVideoByType(Integer typeId,Integer curPage,Integer pageSize,HttpServletRequest request){
        String userId = request.getHeader("userId");
        User user = userMapper.selectByPrimaryKey(Integer.valueOf(userId));
        long total;
        Page page = PageHelper.startPage(curPage,pageSize);
        List<Video> videoList;
        if(typeId==0){
            videoList = videoMapper.getAllVideo();
        }else{
            videoList = videoMapper.getVideoByType(typeId);
        }
        PageInfo<Video> pageInfo = new PageInfo<>(videoList);
        total = pageInfo.getTotal();
        JSONArray jsonArray = new JSONArray();
        for(Video video:videoList){
            JSONObject jsonObject1 = new JSONObject();
            SensitivewordFilter filter = new SensitivewordFilter();
            jsonObject1.put("id",video.getId());
            jsonObject1.put("title",filter.replaceSensitiveWord(video.getTitle(),1,"*"));
            jsonObject1.put("description",filter.replaceSensitiveWord(video.getDescription(),1,"*"));
            jsonObject1.put("picture",video.getPicture());
            jsonObject1.put("url",video.getUrl());
            jsonObject1.put("fileName",video.getFileName());
            jsonObject1.put("author",user.getName());
            jsonObject1.put("typeId",video.getType());
            jsonObject1.put("uploadTime",video.getUploadTime());
            jsonObject1.put("publish",video.getPublish());
            jsonObject1.put("type",typeMapper.getTypeById(video.getType()));
            jsonObject1.put("views",video.getViews());
            jsonObject1.put("tag",tagMapper.getTagById(video.getTag()) );
            jsonArray.add(jsonObject1);
        }
        JSONObject  jsonObject = new JSONObject();
        jsonObject.put("videoList",jsonArray);
        jsonObject.put("total",total);
        return jsonObject;
    }

    public JSONObject getPersonalVideoByTitle(String title, Integer curPage, Integer pageSize, HttpServletRequest request) {
        String userId = request.getHeader("userId");
        User user = userMapper.selectByPrimaryKey(Integer.valueOf(userId));
        long total;
        Page page = PageHelper.startPage(curPage,pageSize);
        List<Video> videoList = videoMapper.getPesonalVideoByTitle(title,user.getId());
        PageInfo<Video> pageInfo = new PageInfo<>(videoList);
        total = pageInfo.getTotal();
        JSONArray jsonArray = new JSONArray();
        for(Video video:videoList){
            JSONObject jsonObject1 = new JSONObject();
            SensitivewordFilter filter = new SensitivewordFilter();
            jsonObject1.put("id",video.getId());
            jsonObject1.put("title",filter.replaceSensitiveWord(video.getTitle(),1,"*"));
            jsonObject1.put("description",filter.replaceSensitiveWord(video.getDescription(),1,"*"));
            jsonObject1.put("picture",video.getPicture());
            jsonObject1.put("url",video.getUrl());
            jsonObject1.put("fileName",video.getFileName());
            jsonObject1.put("author",user.getName());
            jsonObject1.put("typeId",video.getType());
            jsonObject1.put("uploadTime",video.getUploadTime());
            jsonObject1.put("publish",video.getPublish());
            jsonObject1.put("views",video.getViews());
            jsonObject1.put("tag",tagMapper.getTagById(video.getTag()) );
            jsonObject1.put("type",typeMapper.getTypeById(video.getType()));
            jsonArray.add(jsonObject1);
        }
        JSONObject  jsonObject = new JSONObject();
        jsonObject.put("videoList",jsonArray);
        jsonObject.put("total",total);
        return jsonObject;
    }


    //发布
    public int setPublish(Video video){
        return videoMapper.editVideoInfo(video);
    }


    /**
    *@描述 获取发布的视频
    *@创建人 dyj
    *@创建时间 2021/12/2
    **/
    public JSONObject getPublishedVideo(Video video,Integer curPage,Integer pageSize){
        long total;
        Page page = PageHelper.startPage(curPage,pageSize);
        List<Video> videoList = videoMapper.getPublishedVideo(video);
        PageInfo<Video> pageInfo = new PageInfo<>(videoList);
        total = pageInfo.getTotal();
        JSONArray jsonArray = new JSONArray();
        for(Video video1:videoList){
            JSONObject jsonObject1 = new JSONObject();
            SensitivewordFilter filter = new SensitivewordFilter();
            jsonObject1.put("id",video.getId());
            jsonObject1.put("title",filter.replaceSensitiveWord(video.getTitle(),1,"*"));
            jsonObject1.put("description",filter.replaceSensitiveWord(video.getDescription(),1,"*"));
            jsonObject1.put("picture",video1.getPicture());
            jsonObject1.put("url",video1.getUrl());
            jsonObject1.put("fileName",video1.getFileName());
            jsonObject1.put("author",userMapper.selectByPrimaryKey(video1.getAuthor()).getName());
            jsonObject1.put("uploadTime",video1.getUploadTime());
            jsonObject1.put("views",video1.getViews());
            jsonObject1.put("tag",tagMapper.getTagById(video1.getTag()) );
            jsonArray.add(jsonObject1);
        }
        JSONObject  jsonObject = new JSONObject();
        jsonObject.put("videoList",jsonArray);
        jsonObject.put("total",total);
        return jsonObject;
    }

    public int setViews(Long videoId){
        Video record = videoMapper.getVideoById(videoId);
       // System.out.println(record.getId());
        Long views = record.getViews()==null?0:record.getViews();
        record.setViews(views+1);
        return videoMapper.editVideoInfo(record);
    }


    //获取专栏内已发布的视频
    public JSONObject getVideoByCollectionId(Long collectionId,Integer curPage,Integer pageSize,HttpServletRequest request){
        long total;
        Page page = PageHelper.startPage(curPage,pageSize);
        List<Video> videoList = videoMapper.getVideoByCollectionId(collectionId);
        PageInfo<Video> pageInfo = new PageInfo<>(videoList);
        total = pageInfo.getTotal();
        JSONArray jsonArray = new JSONArray();
        for(Video video:videoList){
            JSONObject jsonObject1 = new JSONObject();
            SensitivewordFilter filter = new SensitivewordFilter();
            jsonObject1.put("id",video.getId());
            jsonObject1.put("title",filter.replaceSensitiveWord(video.getTitle(),1,"*"));
            jsonObject1.put("description",filter.replaceSensitiveWord(video.getDescription(),1,"*"));
            jsonObject1.put("picture",video.getPicture());
            jsonObject1.put("url",video.getUrl());
            jsonObject1.put("fileName",video.getFileName());
            jsonObject1.put("author",userMapper.selectByPrimaryKey(video.getAuthor()).getName());
            jsonObject1.put("typeId",video.getType());
            jsonObject1.put("uploadTime",video.getUploadTime());
            jsonObject1.put("views",video.getViews());
            jsonObject1.put("publish",video.getPublish());
            jsonObject1.put("tag",tagMapper.getTagById(video.getTag()) );
            jsonObject1.put("type",typeMapper.getTypeById(video.getType()));
            jsonArray.add(jsonObject1);
        }
        JSONObject  jsonObject = new JSONObject();
        jsonObject.put("videoList",jsonArray);
        jsonObject.put("total",total);
        return jsonObject;
    }


    public JSONObject getAllVideoByCollectionId(Long collectionId, HttpServletRequest request) {
        String userId = request.getHeader("userId");
        User user = userMapper.selectByPrimaryKey(Integer.valueOf(userId));
        List<Video> videoList = videoMapper.getAllVideoByCollectionId(collectionId);
        JSONArray jsonArray = new JSONArray();
        for(Video video:videoList){
            JSONObject jsonObject1 = new JSONObject();
            SensitivewordFilter filter = new SensitivewordFilter();
            jsonObject1.put("id",video.getId());
            jsonObject1.put("title",filter.replaceSensitiveWord(video.getTitle(),1,"*"));
            jsonObject1.put("description",filter.replaceSensitiveWord(video.getDescription(),1,"*"));
            jsonObject1.put("picture",video.getPicture());
            jsonObject1.put("url",video.getUrl());
            jsonObject1.put("fileName",video.getFileName());
            jsonObject1.put("author",user.getName());
            jsonObject1.put("typeId",video.getType());
            jsonObject1.put("uploadTime",video.getUploadTime());
            jsonObject1.put("views",video.getViews());
            jsonObject1.put("publish",video.getPublish());
            jsonObject1.put("tag",tagMapper.getTagById(video.getTag()) );
            jsonObject1.put("type",typeMapper.getTypeById(video.getType()));
            jsonArray.add(jsonObject1);
        }
        JSONObject  jsonObject = new JSONObject();
        jsonObject.put("videoList",jsonArray);
        jsonObject.put("total",jsonArray.size());
        return jsonObject;
    }

    public JSONObject getAllVideoToAudit(Integer curPage,Integer pageSize) {
        long total;
        Page page = PageHelper.startPage(curPage,pageSize);
        List<Video> videoList = videoMapper.getAuditVideo();
        PageInfo<Video> pageInfo = new PageInfo<>(videoList);
        total = pageInfo.getTotal();
        JSONArray jsonArray = new JSONArray();
        for(Video video:videoList){
            JSONObject jsonObject1 = new JSONObject();
            SensitivewordFilter filter = new SensitivewordFilter();
            jsonObject1.put("id",video.getId());
            //待审核的视频不进行关键词过滤
            jsonObject1.put("title",video.getTitle());
            jsonObject1.put("description",video.getDescription());
            jsonObject1.put("picture",video.getPicture());
            jsonObject1.put("url",video.getUrl());
            jsonObject1.put("fileName",video.getFileName());
            jsonObject1.put("author",userMapper.selectByPrimaryKey(video.getAuthor()).getName());
            jsonObject1.put("typeId",video.getType());
            jsonObject1.put("uploadTime",video.getUploadTime());
            jsonObject1.put("views",video.getViews());
            jsonObject1.put("publish",video.getPublish());
            jsonObject1.put("tag",tagMapper.getTagById(video.getTag()) );
            jsonObject1.put("type",typeMapper.getTypeById(video.getType()));
            jsonArray.add(jsonObject1);
        }
        JSONObject  jsonObject = new JSONObject();
        jsonObject.put("videoList",jsonArray);
        jsonObject.put("total",total);
        return jsonObject;

    }
}

