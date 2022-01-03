package com.xxb.mediasystem.mapper;

import com.xxb.mediasystem.model.Video;
import org.apache.ibatis.annotations.Mapper;

import javax.swing.*;
import java.util.List;

@Mapper
public interface VideoMapper {
    int addVideo(Video video);
    List<Video> getAllVideo();
    int editVideoInfo(Video video);
    int deleteVideo(Long videoId);
    int deleteVideoLoji(Long videoId);
    String getPicture(Long videoId);
    List<Video> getVideoByTitle(String title);
    Video getVideoById(Long videoId);
    List<Video> getVideoByType(Integer typeId);
    List<Video> getPersonalVideo(Integer userId);
    List<Video> getPesonalVideoByTitle(String title, Integer userId);
    List<Video> getPublishedVideo(Video video);
    List<Video> getVideoByCollectionId(Long collectionId);
    List<Video> getAllVideoByCollectionId(Long collectionId);
    int setSort(Long videoId,Integer sortId);
    List<Video> getAuditVideo();
}
