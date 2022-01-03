package com.xxb.mediasystem.controller;

import com.xxb.mediasystem.mapper.UserMapper;
import com.xxb.mediasystem.model.Type;
import com.xxb.mediasystem.model.User;
import com.xxb.mediasystem.service.TypeService;
import com.xxb.mediasystem.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
public class TypeController {
    @Autowired
    TypeService typeService;
    @Resource
    UserMapper userMapper;


    @GetMapping("/getTypeList")
    public Result getTypeList(HttpServletRequest request){
        return Result.build(200,"",typeService.getTypeList(request));
    }



    /**
    *@描述 添加分类
    *@创建人 dyj
    *@创建时间 2021/12/2
    **/
    @GetMapping("/addType")
    public Result addType(@RequestParam("typeName")String typeName, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        String userId = request.getHeader("userId");
    //    User user = userMapper.findUserByToken(token);

        Type type = new Type();
        type.setType(typeName);
        type.setAuthor(Integer.valueOf(userId));
        if(typeService.addType(type)==1){
            return Result.build(200,"添加成功");
        }else{
            return Result.build(400,"添加失败");
        }
    }

    /**
    *@描述 编辑分类
    *@创建人 dyj
    *@创建时间 2021/12/2
    **/

    @PostMapping("/editType")
    public Result editType(@RequestParam("typeId") Long id,
                           @RequestParam("typeName")String typeName){
        Type type = new Type();
        type.setType(typeName);
        type.setId(id);
        if(typeService.editType(type)==1){
            return Result.build(200,"修改成功");
        }else{
            return Result.build(400,"修改失败");
        }
    }
    @GetMapping("/deleteType")
    public Result deleteType(@RequestParam("typeId")Long id){
        Type type = new Type();
        type.setId(id);
        if(typeService.deleteType(type)==1){
            return Result.build(200,"删除成功");
        }else{
            return Result.build(400,"删除失败");
        }
    }


}
