package com.xxb.mediasystem.util;

import org.springframework.scheduling.annotation.Async;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.InputFormatException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;

import java.io.File;
import java.util.Date;

public class transferUtil {/**
 * 将avi视频转换为mp4格式

 */

public static void aviToMp4(File source, File target) {
//		File source = new File("/Users/Desktop/aa/111.avi");
//	    File target = new File("/Users/Desktop/aa/111.mp4");
//    File source = new File(oldPath);
//    File target = new File(newPath);
    AudioAttributes audio = new AudioAttributes();
    audio.setCodec("libmp3lame"); //音频编码格式
    audio.setBitRate(new Integer(800000));
    audio.setChannels(new Integer(1));
    //audio.setSamplingRate(new Integer(22050));
    VideoAttributes video = new VideoAttributes();
    video.setCodec("libx264");//视频编码格式
    video.setBitRate(new Integer(3200000));
    video.setFrameRate(new Integer(5));//数字设置小了，视频会卡顿
    EncodingAttributes attrs = new EncodingAttributes();
    attrs.setOutputFormat("mp4");
    attrs.setAudioAttributes(audio);
    attrs.setVideoAttributes(video);
    Encoder encoder = new Encoder();
    MultimediaObject multimediaObject = new MultimediaObject(source);
    try {
        System.out.println("avi转MP4 --- 转换开始:"+new Date());
        encoder.encode(multimediaObject, target, attrs);
        System.out.println(target);
        // 删除avi文件
        source.delete();

        System.out.println("avi转MP4 --- 转换结束:"+new Date());
    } catch (IllegalArgumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (InputFormatException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    } catch (EncoderException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
}


}
