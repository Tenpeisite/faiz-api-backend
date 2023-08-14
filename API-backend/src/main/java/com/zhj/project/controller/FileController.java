package com.zhj.project.controller;

import com.zhj.common.utils.BaseResponse;
import com.zhj.project.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author zhj
 * @version 1.0
 * @date 2023/7/12 23:02
 */
@RequestMapping("/file")
@RestController
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public BaseResponse<String> uploadImg(@RequestParam("file") MultipartFile file) {
        try {
            return fileService.uploadImg(file);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件上传上传失败");
        }
    }
}
