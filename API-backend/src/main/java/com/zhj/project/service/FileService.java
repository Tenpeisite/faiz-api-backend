package com.zhj.project.service;

import com.zhj.common.utils.BaseResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/7/12 23:09
 */
public interface FileService {
    BaseResponse<String> uploadImg(MultipartFile file);
}
