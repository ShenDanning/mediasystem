package com.xxb.mediasystem.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.xxb.mediasystem.util.FileUtil;
import com.xxb.mediasystem.util.transferUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FdfsService {
    @Autowired
    private FastFileStorageClient fastFileStorageClient;


    public String upToFdfs(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String extName = FileUtil.getExtensionName(fileName);
        // InputStream in = file.getInputStream();
        System.out.println(extName);
        if(extName.equals("avi")){
            //将MutipartFile转成sourceFile
            File sourceFile = null;
            try {
                sourceFile = FileUtil.multipartFileToFile(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //建一个临时的文件
            File targetFile = new File("/opt/video/temp.mp4");
            //转换
            transferUtil.aviToMp4(sourceFile,targetFile);
            //获取输入流
            InputStream in = null;
            in = new FileInputStream(targetFile);
            extName = "mp4";
            StorePath storePath = fastFileStorageClient.uploadFile(in,file.getSize(), extName,null);
            targetFile.delete();
            return storePath.getFullPath();
        }else{
            InputStream in = file.getInputStream();
            StorePath storePath = fastFileStorageClient.uploadFile(in,file.getSize(),extName,null);
            return storePath.getFullPath();
        }

        //System.out.println(storePath.getFullPath());

    }
    public void deleteFile(String avatarPath){
        StorePath storePath = StorePath.praseFromUrl(avatarPath);
        System.out.println(storePath.getGroup()+storePath.getPath());
       fastFileStorageClient.deleteFile(storePath.getGroup(),storePath.getPath());
    }
}
