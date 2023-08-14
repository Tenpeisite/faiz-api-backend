package com.zhj.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhj.common.model.entity.InterfaceInfo;


/**
* @author 鏈辩剷鏉�
* @description 针对表【interface_info(接口信息)】的数据库操作Mapper
* @createDate 2023-04-03 22:17:53
* @Entity com.yupi.project.com.yupi.apicommon.model.entity.InterfaceInfo
*/
public interface InterfaceInfoMapper extends BaseMapper<InterfaceInfo> {


    String getInterfaceUrl(String path);
}
