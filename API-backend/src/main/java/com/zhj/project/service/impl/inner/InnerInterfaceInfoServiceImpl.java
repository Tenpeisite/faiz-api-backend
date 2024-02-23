package com.zhj.project.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhj.common.model.entity.InterfaceInfo;
import com.zhj.common.utils.ErrorCode;
import com.zhj.project.exception.BusinessException;
import com.zhj.project.mapper.InterfaceInfoMapper;
import com.zhj.common.service.InnerInterfaceInfoService;
import com.zhj.project.mapper.UserMapper;
import com.zhj.project.service.InterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/4/10 22:01
 */
@DubboService
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {

    @Autowired
    private InterfaceInfoMapper interfaceInfoMapper;

    @Autowired
    private InterfaceInfoService interfaceInfoService;

    @Override
    public InterfaceInfo getInterfoceInfo(String methodName, String method) {
        if (StringUtils.isAnyBlank(methodName, method)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<InterfaceInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InterfaceInfo::getMethodName, methodName).eq(InterfaceInfo::getMethod, method);
        return interfaceInfoMapper.selectOne(queryWrapper);
    }

    @Override
    public String getInterfaceUrl(String methodName) {
        return interfaceInfoMapper.getInterfaceUrl(methodName);
    }



    @Override
    public boolean invokeCount(Long interfaceId,Long userId) {
        return interfaceInfoService.invokeCount(interfaceId,userId);
    }
}
