package com.zhj.project.controller;

import com.zhj.common.model.dto.file.ImageVo;
import com.zhj.common.model.dto.file.UploadFileRequest;
import com.zhj.common.model.entity.User;
import com.zhj.common.model.enums.FileUploadBizEnum;
import com.zhj.common.model.vo.UserVO;
import com.zhj.common.utils.BaseResponse;
import com.zhj.common.utils.ResultUtils;
import com.zhj.project.service.FileService;
import com.zhj.project.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


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

    @Resource
    private UserService userService;

    @PostMapping("/upload")
    public BaseResponse<ImageVo> uploadImg(@RequestParam("file") MultipartFile multipartFile, UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        try {
            String url = fileService.uploadImg(multipartFile).getData();
            //用户头像
            if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
                UserVO loginUser = userService.getLoginUser(request);
                loginUser.setUserAvatar(url);
                User user = new User();
                user.setId(loginUser.getId());
                user.setUserAvatar(url);
                userService.updateById(user);
            }
            ImageVo imageVo = new ImageVo();
            //imageVo.setUid();
            //imageVo.setName(multipartFile.getOriginalFilename());
            //imageVo.setStatus();
            imageVo.setUrl(url);
            return ResultUtils.success(imageVo);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件上传上传失败");
        }
    }
}
