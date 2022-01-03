package com.xxb.mediasystem.mapper;

import com.xxb.mediasystem.model.Type;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TypeMapper {
    List<Type> getTypeList(Integer userId);
    String getTypeById(Integer typeId);
    int addType(Type type);
    int editType(Type type);
    int deleteType(Type type);
}
