package com.xxb.mediasystem.mapper;

import com.xxb.mediasystem.model.Collections;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CollectionsMapper {
    int addCollection(Collections collections);
    List<Collections> getCollections(Integer userId);
    int editCollections(Collections collections);
    Collections getCollectionById(Long Id);
    int deleteCollection(Long id);
    List<Collections> getPublishedCollections();
    List<Collections> getPublishedCollectionsByName(String name);
    List<Collections> getAuditCollections();
}
