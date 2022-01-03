package com.xxb.mediasystem.controller;

import com.alibaba.fastjson.JSONObject;
import com.xxb.mediasystem.model.Video;
import com.xxb.mediasystem.service.FdfsService;
import com.xxb.mediasystem.service.VideoService;
import com.xxb.mediasystem.util.FileUtil;
import com.xxb.mediasystem.util.Result;
import com.xxb.mediasystem.util.transferUtil;
import org.mockito.internal.matchers.ArrayEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * 视频广场页面
 */
@RestController
public class VideoController {
    @Autowired
    private FdfsService fdfsService;
    @Value("${file.upload.abpath}")
    private String abpath;
    //上传图片Url
    @Value("${file.upload.mdImageDir}")
    private String mdImageDir;
    //上传视频Url
    @Value("${file.upload.mdImageDir}")
    private String mdVideoDir;

    @Value("${file.upload.serverUrl}")
    private String imgUrlDir;


    @Autowired
    private VideoService videoService;



    @GetMapping("/getVideoByType")
    public Result getVideoByType(@RequestParam(value = "curPage",defaultValue = "-1")Integer curPage,
                                 @RequestParam(value = "pageSize",defaultValue = "-1")Integer pageSize,
                                 @RequestParam(value = "typeId",defaultValue = "-1")Integer typeId,
                                 @RequestParam(value = "title",defaultValue = "-1")String title,
                                 HttpServletRequest request){
        JSONObject jsonObject = new JSONObject();
        if(title.equals("-1")){
            jsonObject = videoService.getVideoByType(typeId,curPage,pageSize,request);
        }else {//如果有title则是根据标题搜索
           jsonObject = videoService.getVideoByTitle(title,curPage,pageSize,request);
        }
        return Result.build(200,"",jsonObject);
    }
    @GetMapping("/getVideoByTitle")
    public Result getVideoByTitle(@RequestParam(value = "curPage",defaultValue = "-1")Integer curPage,
                                  @RequestParam(value = "pageSize",defaultValue = "-1")Integer pageSize,
                                  @RequestParam(value = "title",defaultValue = "-1")String title,
                                  HttpServletRequest request){
        JSONObject jsonObject = new JSONObject();
        if(title.equals("-1")){//如果没有Title,获取全部
            jsonObject = videoService.getAllVideo(curPage,pageSize);
        }else {
            jsonObject = videoService.getVideoByTitle(title,curPage,pageSize,request );
        }
        return Result.build(200,"",jsonObject);
    }

    @PostMapping("/editPicture")
    public Result editPicture(@RequestParam("videoId") Long videoId,
                              @RequestParam("file") MultipartFile file,HttpServletRequest request){
        Video video  = new Video();
        String imgAbPath = abpath + "/assets/";// /home/blogtest/assets/
        //返回对应的File类型f
        File img = FileUtil.upload(file, imgAbPath);
        video.setPicture(imgUrlDir + "/" + img.getName());
//        try {
//
//            String picturePath=fdfsService.upToFdfs(file);
//            video.setPicture("http://1.15.227.166:8888/"+picturePath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        video.setId(videoId);
        int res = videoService.editVideoInfo(video,1);
        if(res==1){
            return Result.build(200,"修改成功");
        }else{
            return Result.build(400,"修改失败");
        }
    }

    @PostMapping("/editPictureTofdfs")
    public Result editPictureTofdfs(@RequestParam("videoId") Long videoId,
                              @RequestParam("file") MultipartFile file,HttpServletRequest request){
        Video video  = new Video();
        try {

            String picturePath=fdfsService.upToFdfs(file);
            video.setPicture(imgUrlDir+'/'+picturePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        video.setId(videoId);
        int res = videoService.editVideoInfofdfs(video,1);
        if(res==1){
            return Result.build(200,"修改成功");
        }else{
            return Result.build(400,"修改失败");
        }
    }

    @PostMapping("/editVideoInfo")
    public Result editVideoInfo(@RequestParam("videoId") Long videoId,
                                @RequestParam("description")String description,
                                @RequestParam("title")String title,
                                @RequestParam(value = "type",defaultValue = "0")Integer typeId, HttpServletRequest request){
        Video video  = new Video();
//        try {
//            String picturePath=fdfsService.upToFdfs(file);
//            video.setPicture("http://1.15.227.166:8888/"+picturePath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        video.setId(videoId);
        video.setTitle(title);
        video.setDescription(description);
        video.setType(typeId);
        int res = videoService.editVideoInfo(video,2);//flag=1 表示更改图片
        if(res==1){
            return Result.build(200,"修改成功");
        }else{
            return Result.build(400,"修改失败");
        }
    }

    @GetMapping("/deleteVideo")
    public Result deleteVideo(@RequestParam(value="videoId",defaultValue = "-1")Long videoId){

        int res = videoService.deleteVideoLoji(videoId);

        if(res==1){
            return Result.build(200,"删除成功");
        }else{
            return Result.build(400,"删除失败");
        }

    }
    /**
    *@描述 分享一个视频
    *@创建人 dyj
    *@创建时间 2021/12/2
    **/
    @PostMapping("/setPublish")
    public Result setPublish(@RequestParam("videoId")Long videoId,
                             @RequestParam("publish")Integer publish,
                             @RequestParam("tag")Integer tagId){
        Video video= new Video();
        video.setId(videoId);
        video.setPublish(publish);
        video.setTag(tagId);
        int res = videoService.setPublish(video);
        if(res==1){
            return Result.build(200,"发布成功");
        }else{
            return Result.build(400,"发布失败");
        }
    }


    @GetMapping("/public/getPublishedVideo")
    public Result getPublishedVideo(@RequestParam(value = "title",defaultValue = "")String title,
                                    @RequestParam("curPage")Integer curPage,
                                    @RequestParam("pageSize")Integer pageSize,
                                    @RequestParam(value = "tag",defaultValue = "")Integer tag){
        Video video = new Video();
        video.setTitle(title);
        video.setTag(tag);
        JSONObject jsonObject = new JSONObject();
        jsonObject = videoService.getPublishedVideo(video,curPage,pageSize);
        return Result.build(200,"",jsonObject);
    }

    @GetMapping("/public/setViews")
    public Result setViews(@RequestParam("videoId")Long videoId){
        if(videoService.setViews(videoId)==1){
            return Result.build(200,"浏览次数+1");
        }else{
            return Result.build(400,"失败");
        }
    }

    @PostMapping("/public/uploadFile")
    public Result uploadFile(@RequestParam("image") MultipartFile file, HttpServletRequest request) {
//        String token = TokenUtil.getRequestToken(request);
//        User user = authService.findByToken(token);
        try {
            String avatarPath=fdfsService.upToFdfs(file);
            System.out.println(avatarPath);
            // authService.editAvatar(avatarPath,user.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.build(200,"上传成功");
    }

    @GetMapping("/getVideoByCollectionId")
    public Result getVideoByCollectionId(@RequestParam("collectionId")Long collectionId,
                                         @RequestParam("curPage")Integer curPage,
                                         @RequestParam("pageSize")Integer pageSize,
                                         HttpServletRequest request){
        JSONObject jsonObject = new JSONObject();
        jsonObject = videoService.getVideoByCollectionId(collectionId,curPage,pageSize,request);
        return Result.build(200,"",jsonObject);
    }





}
