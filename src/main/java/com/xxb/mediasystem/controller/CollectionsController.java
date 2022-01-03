package com.xxb.mediasystem.controller;

import com.alibaba.fastjson.JSONObject;
import com.xxb.mediasystem.mapper.CollectionsMapper;
import com.xxb.mediasystem.mapper.VideoMapper;
import com.xxb.mediasystem.model.Collections;
import com.xxb.mediasystem.model.Video;
import com.xxb.mediasystem.service.CollectionsService;
import com.xxb.mediasystem.service.FdfsService;
import com.xxb.mediasystem.service.VideoService;
import com.xxb.mediasystem.util.FileUtil;
import com.xxb.mediasystem.util.Result;
import org.mockito.internal.matchers.ArrayEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Date;

@RestController
@RequestMapping(value = "collections")
public class CollectionsController {
    @Autowired
    private CollectionsService collectionsService;
    @Autowired
    private VideoService videoService;
    @Autowired
    private FdfsService fdfsService;
    @Resource
    private VideoMapper videoMapper;
    @Resource
    private CollectionsMapper collectionsMapper;
    @Value("${file.upload.abpath}")
    private String abpath;
    @Value("${file.upload.mdImageDir}")
    private String mdImageDir;
    //上传视频Url
    @Value("${file.upload.mdImageDir}")
    private String mdVideoDir;
    //

    //端口号
    @Value("${server.port}")
    private String port;
    @Value("${file.upload.serverUrl}")
    private String imgUrlDir;


    @PostMapping("/add")
    public Result addCollections(@RequestParam("name")String name,
                                 @RequestParam("picture") MultipartFile picture,
                                 @RequestParam(value = "description",required = false)String description,
                                 HttpServletRequest request){
        Collections collections = new Collections();
        String userId = request.getHeader("userId");
        collections.setAuthor(Integer.valueOf(userId));
        collections.setName(name);
        Date now = new Date();
        collections.setCreatedTime(now);
        //上传封面
        String imgAbPath = abpath + "/assets/";
        //本地测试用
       // String imgUrlDir = "http:" + request.getHeader("Origin").split(":")[1] + ":" + port + "/v1" + mdImageDir;
        //部署时用
        String imgUrlDir = "http://10.10.22.106";
        File img = FileUtil.upload(picture, imgAbPath);
        collections.setPicture(imgUrlDir+"/"+img.getName());
        collections.setDescription(description);
        if(collectionsService.addCollection(collections)==1){
            return Result.build(200,"添加成功");
        }else{
            return Result.build(400,"添加失败");
        }
    }

    //获取个人所有的专栏

    @GetMapping("/getAll")
    public Result getAllCollections(@RequestParam(value = "curPage",defaultValue = "-1")Integer curPage,
                                    @RequestParam(value = "pageSize",defaultValue = "-1")Integer pageSize,
                                    HttpServletRequest request){
        JSONObject jsonObject = new JSONObject();
        jsonObject = collectionsService.getCollections(curPage,pageSize,request);
        return Result.build(200,"",jsonObject);
    }

    @PostMapping("/edit")
    public Result editCollections(@RequestParam(value = "collectionId",required = true) Long collectionId,
                                  @RequestParam(value = "name",required = false)String name,
                                  @RequestParam(value = "picture",required = false)MultipartFile picture,
                                  @RequestParam(value = "description",required = false)String description,
                                  HttpServletRequest request){
        Collections collections = new Collections();
        collections.setId(collectionId);
        collections.setName(name);
        collections.setDescription(description);
        int isImgEidt=0;
        if(picture!=null){
            isImgEidt = 1;
            //上传封面
            String imgAbPath = abpath + "/assets/";
            File img = FileUtil.upload(picture, imgAbPath);
            collections.setPicture(imgUrlDir+"/"+img.getName());
        }
        int res = collectionsService.editCollections(collections,isImgEidt);
        if(res == 1){
            return Result.build(200,"编辑成功");
        }else{
            return Result.build(400,"编辑失败");
        }
    }

    @PostMapping("/editTofdfs")
    public Result editCollectionsTofdfs(@RequestParam(value = "collectionId",required = true) Long collectionId,
                                  @RequestParam(value = "name",required = false)String name,
                                  @RequestParam(value = "picture",required = false)MultipartFile picture,
                                  @RequestParam(value = "description",required = false)String description,
                                  HttpServletRequest request){
        Collections collections = new Collections();
        collections.setId(collectionId);
        collections.setName(name);
        collections.setDescription(description);
        int isImgEidt=0;
        if(picture!=null){
            isImgEidt = 1;
            //上传封面并保存至数据库
            try {
                String picturePath = fdfsService.upToFdfs(picture);
                collections.setPicture(imgUrlDir+'/'+picturePath);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        int res = collectionsService.editCollectionsTofdfs(collections,isImgEidt);
        if(res == 1){
            return Result.build(200,"编辑成功");
        }else{
            return Result.build(400,"编辑失败");
        }
    }

    @GetMapping("/delete")
    public Result deleteCollection(@RequestParam(value = "collectionId")Long collectionId){
        int res = collectionsService.deleteCollection(collectionId);
        if(res ==1){
            return Result.build(200,"删除成功");
        }else{
            return Result.build(400,"删除失败");
        }

    }

    @PostMapping("/uploadVideo")
    public Result uploadFile(@RequestParam(value ="file",defaultValue = "-1")  MultipartFile file,
                             @RequestParam(value = "description",required = false)String description,
                             @RequestParam("title")String title,
                             @RequestParam("collectionId")Long collectionId,
                             HttpServletRequest request) {

        //绝对路径
        String AbPath = abpath;
        //上传视频
        File f = FileUtil.upload(file, AbPath);
        //将视频信息存储至数据库
        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        String userId = request.getHeader("userId");
        video.setAuthor(Integer.valueOf(userId));
        video.setPicture(collectionsMapper.getCollectionById(collectionId).getPicture());
        video.setFileName(f.getName());
        Date now = new Date();
        video.setUploadTime(now);
        video.setPath(AbPath);
        video.setUrl(imgUrlDir+"/"+f.getName());
        video.setCollectionId(collectionId);
        video.setType(0);
        int curNum = videoMapper.getAllVideoByCollectionId(collectionId).size();
        video.setSortId(curNum+1);
        if(videoService.addVideo(video)==1){
            Collections collections = collectionsMapper.getCollectionById(collectionId);
            collections.setId(collectionId);
            collections.setVideos(curNum+1);
            collectionsService.editCollections(collections,2);
            return Result.build(200, "保存成功");
        }else {
            return Result.build(400,"上传失败");
        }
    }
    @PostMapping("/uploadVideoTofdfs")
    public Result uploadFileTofdfs(@RequestParam(value ="file",defaultValue = "-1")  MultipartFile file,
                             @RequestParam(value = "description",required = false)String description,
                             @RequestParam("title")String title,
                             @RequestParam("collectionId")Long collectionId,
                             HttpServletRequest request) {

        String url=imgUrlDir+'/';
        Video video= new Video();
        //上传视频
        try {
            String videoPath=fdfsService.upToFdfs(file);
            video.setUrl(url+videoPath);
            video.setPath(videoPath);
            video.setFileName(file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        video.setTitle(title);
        video.setDescription(description);
        String userId = request.getHeader("userId");
        video.setAuthor(Integer.valueOf(userId));
        video.setPicture(collectionsMapper.getCollectionById(collectionId).getPicture());
        video.setFileName(file.getName());
        Date now = new Date();
        video.setUploadTime(now);
        video.setCollectionId(collectionId);
        video.setType(0);
        if(videoService.addVideo(video)==1){
            Collections collections = collectionsMapper.getCollectionById(collectionId);
            collections.setId(collectionId);
            int num = videoMapper.getVideoByCollectionId(collectionId).size();
            System.out.println(num);
            collections.setVideos(num);
            collectionsService.editCollectionsTofdfs(collections,2);
            return Result.build(200, "保存成功");
        }else {
            return Result.build(400,"上传失败");
        }
    }


    //获取发布
    @GetMapping("/getPublished")
    public Result getPublishedCollections(@RequestParam(value = "name",required = false)String name,
                                          @RequestParam(value = "curPage")Integer curPage,
                                          @RequestParam(value = "pageSize")Integer pageSize){
        JSONObject jsonObject = new JSONObject();
        if(name==null) {
            jsonObject = collectionsService.getPublishedCollections(curPage, pageSize);

        }else{
            jsonObject = collectionsService.getPublishedCollectionsByName(name,curPage,pageSize);
        }
        return Result.build(200, "", jsonObject);
    }
    @GetMapping("/getAudit")
    public Result getAuditCollections(@RequestParam(value = "curPage")Integer curPage,
                                      @RequestParam(value = "pageSize")Integer pageSize){
        JSONObject jsonObject = new JSONObject();
        jsonObject = collectionsService.getAuditCollections(curPage,pageSize);
        return Result.build(200, "", jsonObject);
    }


    @PostMapping("/publish")
    public Result publishCollections(@RequestParam(value = "collectionId",required = true)Long collectionId,
                                     @RequestParam(value = "publish",required = true)Integer publish){
        Collections collections = new Collections();
        collections.setId(collectionId);
        collections.setPublish(publish);
         if(collectionsService.setPublish(collections)==1){
             return Result.build(200,"发布成功");
         }else {
             return Result.build(400,"发布失败");
         }
    }

    @GetMapping("/setUp")
    public Result setUp(@RequestParam(value = "videoId",required = true)Long videoId){
        return collectionsService.setUp(videoId);
    }
    @GetMapping("/setDown")
    public Result setDown(@RequestParam(value = "videoId",required = true)Long videoId){
        return collectionsService.setDown(videoId);
    }












}
