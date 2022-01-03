package com.xxb.mediasystem.controller;

import cn.yueshutong.springbootstartercurrentlimiting.annotation.CurrentLimiter;
import com.alibaba.fastjson.JSONObject;
import com.xxb.mediasystem.mapper.UserMapper;
import com.xxb.mediasystem.model.Video;
import com.xxb.mediasystem.service.FdfsService;
import com.xxb.mediasystem.service.VideoService;
import com.xxb.mediasystem.util.FileUtil;
import com.xxb.mediasystem.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * 视频管理页面的接口
 */
@RestController
@EnableAsync
public class PersonalVideoController {

    @Resource
    private UserMapper userMapper;
    @Autowired
    private VideoService videoService;

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

    //端口号
    @Value("${server.port}")
    private String port;

    @Value("${file.upload.serverUrl}")
    private String imgUrlDir;

    @GetMapping("/test")
    public void publish() {
        System.out.println("1");
    }


    @PostMapping("/uploadVideo")
    @CurrentLimiter(QPS=5)
    public Result uploadFile(@RequestParam(value ="file",defaultValue = "-1")  MultipartFile file,
                             @RequestParam(value = "picture",required = false) MultipartFile picture,
                             @RequestParam("description")String description,
                             @RequestParam("title")String title,
                             @RequestParam(value = "typeId",defaultValue = "0")Integer typeId,
                             HttpServletRequest request) {//加用户Id

        return  videoService.uploadVideo(file,picture,description,title,typeId,request);

//        //绝对路径
//        String AbPath = abpath;
//        //上传视频
//        File f = FileUtil.upload(file, AbPath);
//        //将视频信息存储至数据库
//        Video video = new Video();
//        video.setTitle(title);
//        video.setDescription(description);
//        video.setType(typeId);
//        video.setCollectionId(Long.valueOf(0));
//        //String token = request.getHeader("Authorization");
//        String userId = request.getHeader("userId");
//        //video.setAuthor(userMapper.findUserByToken(token).getId());
//        video.setAuthor(Integer.valueOf(userId));
//        String imgAbPath = abpath + "/assets/";
//        //返回对应的File类型f
//        File img = FileUtil.upload(picture, imgAbPath);
////        try {//封面上传至fastdfs
////            String picturePath=fdfsService.upToFdfs(picture);
////            video.setPicture(imgUrlDir+'/'+picturePath);
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//        video.setFileName(f.getName());
//        Date now = new Date();
//        video.setUploadTime(now);
//        video.setPath(AbPath);
//        video.setUrl(imgUrlDir+"/"+f.getName());
//        video.setPicture(imgUrlDir+'/'+img.getName());
//
//        if(videoService.addVideo(video)==1){
//            return Result.build(200, "保存成功");
//        }else {
//            return Result.build(400,"上传失败");
//        }
    }

    @PostMapping("/uploadVideoTofdfs")
    public Result uploadFileTofdfs(@RequestParam(value ="file",defaultValue = "-1")  MultipartFile file,
                             @RequestParam("picture") MultipartFile picture,
                             @RequestParam("description")String description,
                             @RequestParam("title")String title,
                             @RequestParam(value = "typeId",defaultValue = "0")Integer typeId,
                             HttpServletRequest request) {//加用户Id

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
        //上传封面
        try {
            String picturePath=fdfsService.upToFdfs(picture);
            video.setPicture(url+picturePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //将视频信息存储至数据库
        video.setTitle(title);
        video.setDescription(description);
        video.setType(typeId);
        String userId = request.getHeader("userId");
        video.setAuthor(Integer.valueOf(userId));
        Date now = new Date();
        video.setUploadTime(now);
        if(videoService.addVideo(video)==1){
            return Result.build(200, "保存成功");
        }else {
            return Result.build(400,"上传失败");
        }
    }

    @GetMapping("/personal/getAllVideo")
    public Result getAllVideo(@RequestParam(value ="pageSize",defaultValue = "-1") Integer pageSize,
                              @RequestParam(value ="curPage",defaultValue = "-1") Integer curPage,
                              HttpServletRequest request
                       ){
        JSONObject jsonObject = new JSONObject();
        jsonObject = videoService.getPersonalVideo(curPage,pageSize,request);
        return Result.build(200,"",jsonObject);

    }



    @GetMapping("/personal/getVideoByTitle")
    public Result getVideoByTitle(@RequestParam(value = "curPage",defaultValue = "-1")Integer curPage,
                                  @RequestParam(value = "pageSize",defaultValue = "-1")Integer pageSize,
                                  @RequestParam(value = "title",defaultValue = "-1")String title,
                                  HttpServletRequest request){
        JSONObject jsonObject = new JSONObject();
        if(title.equals("-1")){//如果没有Title,获取全部
            jsonObject = videoService.getPersonalVideo(curPage,pageSize,request);
        }else {
            jsonObject = videoService.getPersonalVideoByTitle(title,curPage,pageSize,request);
        }
        return Result.build(200,"",jsonObject);
    }

    @GetMapping("/personal/getAllVideoByCollectionId")
    public Result getAllVideoByCollectionId(@RequestParam("collectionId")Long collectionId,
                                            HttpServletRequest request){
        JSONObject jsonObject = new JSONObject();
        jsonObject = videoService.getAllVideoByCollectionId(collectionId,request);
        return Result.build(200,"",jsonObject);
    }


}
