package com.xxb.mediasystem.controller;

import com.alibaba.fastjson.JSONObject;
import com.xxb.mediasystem.service.UserService;
import com.xxb.mediasystem.service.VideoService;
import com.xxb.mediasystem.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "admin")
public class AdminController {
    @Autowired
    private VideoService videoService;
    @Autowired
    private UserService userService;

    @GetMapping("/getAllVideoToAudit")
    public Result getAllVideoToAudit(@RequestParam(value = "curPage",required = true)Integer curPage,
                                     @RequestParam(value = "pageSize",required = true)Integer pageSize){
        JSONObject jsonObject = new JSONObject();
        jsonObject = videoService.getAllVideoToAudit(curPage,pageSize);
        return Result.build(200,"",jsonObject);
    }

    @GetMapping("/getAllUsers")
    public Result getAllUsers(@RequestParam(value = "type",required = false)Integer type,
                              @RequestParam(value = "curPage",required = true)Integer curPage,
                              @RequestParam(value = "pageSize",required = true)Integer pageSize){
        JSONObject jsonObject = new JSONObject();
        if(type!=null){
            jsonObject = userService.getAllUsersByType(type,curPage,pageSize);
        }else{
            jsonObject = userService.getAllUsers(curPage,pageSize);
        }
        return Result.build(200,"",jsonObject);
    }

    @GetMapping("/setUserType")
    public Result setUserType(@RequestParam(value = "userId")Integer userId,
                              @RequestParam(value = "type")Integer type){
        int res = userService.editUserType(userId,type);
        if(res == 1){
            return Result.build(200,"修改成功");
        }else{
            return Result.build(400,"修改失败");
        }
    }


}
