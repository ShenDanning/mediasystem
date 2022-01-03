package com.xxb.mediasystem.controller;

import com.alibaba.fastjson.JSONObject;
import com.xxb.mediasystem.service.TagService;
import com.xxb.mediasystem.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "public")
public class TagController {
    @Autowired
    private TagService tagService;

    @GetMapping("/getTagList")
    public Result getTypeList(HttpServletRequest request){
        return Result.build(200,"",tagService.getTagList());
    }


}
