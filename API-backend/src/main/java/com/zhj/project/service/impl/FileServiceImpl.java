package com.zhj.project.service.impl;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.zhj.common.utils.BaseResponse;
import com.zhj.common.utils.ErrorCode;
import com.zhj.common.utils.PathUtils;
import com.zhj.common.utils.ResultUtils;
import com.zhj.project.exception.BusinessException;
import com.zhj.project.service.FileService;
import lombok.Data;
import org.omg.CORBA.SystemException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/7/12 23:10
 */
@Service
@Data
@ConfigurationProperties(prefix = "oss")
public class FileServiceImpl implements FileService {

    private String accessKey;
    private String secretKey;
    private String bucket;
    private String website;

    @Override
    public BaseResponse<String> uploadImg(MultipartFile file) {
        // 判断文件类型或者文件大小
        //获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (!originalFilename.endsWith(".jpg") && !originalFilename.endsWith(".png") && !originalFilename.endsWith(".jpeg")) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //如果判断通过上传文件到oss
        String filePath = PathUtils.generateFilePath(originalFilename);
        //redisTemplate.opsForSet().add(SystemConstants.Article_PIC_RESOURCES, filePath);
        String url = uploadOss(file, filePath);
        return ResultUtils.success(url);
    }

    public String uploadOss(MultipartFile imgFile, String filePath) {
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.region2());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
        //...其他参数参考类注释

        UploadManager uploadManager = new UploadManager(cfg);
        //...生成上传凭证，然后准备上传

        //默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = filePath;

        try {
            InputStream inputStream = imgFile.getInputStream();
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket);

            try {
                Response response = uploadManager.put(inputStream, key, upToken, null, null);
                //解析上传成功的结果
                DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                System.out.println(putRet.key);
                System.out.println(putRet.hash);
                return website + key;
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch (QiniuException ex2) {
                    //ignore
                }
            }
        } catch (Exception ex) {
            //ignore
        }
        return "www";
    }
}
