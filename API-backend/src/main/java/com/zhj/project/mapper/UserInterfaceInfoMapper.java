package com.zhj.project.mapper;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhj.common.model.entity.UserInterfaceInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 鏈辩剷鏉�
 * @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Mapper
 * @createDate 2023-04-06 15:13:13
 * @Entity com.yupi.project.com.yupi.apicommon.model.entity.UserInterfaceInfo
 */
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {

    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(@Param("limit") int limit);

    List<UserInterfaceInfo> listAllInterfaceInfo();

    List<UserInterfaceInfo> listAllLeftCount();

    void changeCount(@Param("userId") Long userId, @Param("interfaceInfoId") Long interfaceInfoId);

    Long getCountByInterfaceInfoId(Long interfaceInfoId);

}
