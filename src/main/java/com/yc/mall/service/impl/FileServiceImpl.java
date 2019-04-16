package com.yc.mall.service.impl;

import com.google.common.collect.Lists;
import com.yc.mall.service.IFileService;
import com.yc.mall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file, String path){
        String fileName = file.getOriginalFilename();
        //扩展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        //因为两个用户上传的文件名可能相同，所以在文件名前面加上UUID，让所有文件都不相同。。。好暴力
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
        logger.info("开始上传文件，上传文件的文件名:{}, 上传的路径:{}, 新文件名:{}", fileName, path, fileExtensionName);

        File fileDir = new File(path);
        //file.mkdirs() 创建目录
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        //创建文件
        File targetFile = new File(path, uploadFileName);
        //上传文件替换目标文件
        try {
            //文件上传成功
            file.transferTo(targetFile);
            //将文件上传到ftp服务器上
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            //将upload下面的文件删除
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件异常", e);
            return null;
        }
        return targetFile.getName();
    }

}
