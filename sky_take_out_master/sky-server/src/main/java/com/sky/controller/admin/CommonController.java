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
 * 通用接口
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
     public Result<String> upload(MultipartFile file){
         log.info("文件上传：{}",file);
         try {
             //为了保证传上去的文件名是唯一的，这里要对文件名进行一定的处理
             //获得原始的文件名
             String originalFileName=file.getOriginalFilename();
             //截取原始文件名的后缀
             String substring = originalFileName.substring(originalFileName.lastIndexOf("."));
             String objectName=UUID.randomUUID().toString()+substring;
             //文件请求路径
             String filePath=aliOssUtil.upload(file.getBytes(),objectName);
             return Result.success(filePath);
         } catch (IOException e) {
             log.error("文件上传失败:{}",e);
         }
         return Result.error(MessageConstant.UPLOAD_FAILED);
     }
}
