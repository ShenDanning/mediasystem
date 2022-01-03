package com.xxb.mediasystem.mapper;

import com.xxb.mediasystem.model.PublicTag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PublicTagMapper {
    List<PublicTag> getTagList();
    String getTagById(Integer tagId);

}
