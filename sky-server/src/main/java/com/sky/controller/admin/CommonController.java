package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 *User:我吃饭的时候不饿
 *Date: 2023/11/01 20:18
 *Description:文件上传接口
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传, {}" + file);

        //获取原文件名
        String originalFilename = file.getOriginalFilename();
        //获取原文件名的后缀名（文件类型）  aaabbb.txt --->  获取txt
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        //构造新文件名
        String objectFileName = UUID.randomUUID().toString() + extension;

        //文件的请求路径
        try {
            String upload = aliOssUtil.upload(file.getBytes(), objectFileName);
            return Result.success(upload);
        } catch (IOException e) {
            log.info("文件上传失败: {}" + e);
        }

        //文件上传失败
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }

}
