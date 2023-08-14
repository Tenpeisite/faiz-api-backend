package com.zhj.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zhj.common.model.entity.InterfaceInfo;

public interface InterfaceInfoService extends IService<InterfaceInfo> {
    void validInterfaceInfo(InterfaceInfo post, boolean add);

    boolean saveInterface(InterfaceInfo interfaceInfo);

    boolean removeInterfaceById(long id);

    boolean updateInterfaceById(InterfaceInfo interfaceInfo);

    InterfaceInfo getInterfaceById(long id);
}