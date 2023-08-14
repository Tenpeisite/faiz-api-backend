package com.zhj.project.service.impl.inner;


import com.zhj.project.service.UserInterfaceInfoService;
import com.zhj.common.service.InnerUserInterfaceInfoService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/4/10 22:02
 */
@DubboService
public class InnerUserInterfaceInfoServiceInfo implements InnerUserInterfaceInfoService {
    @Autowired
    private UserInterfaceInfoService userInterfaceInfoService;

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        return userInterfaceInfoService.invokeCount(interfaceInfoId, userId);
    }

    @Override
    public boolean isNumOfUse(Long id, Long userId) {
        return userInterfaceInfoService.isNumOfUse(id,userId);
    }
}
