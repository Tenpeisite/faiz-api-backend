package com.zhj.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zhj.common.model.entity.UserInterfaceInfo;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/4/10 22:08
 */
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);

    boolean invokeCount(long interfaceInfoId, long userId);

    void changeCount(Long userId, Long interfaceInfoId);

    void updateInterfaceRankList(Long userId, Long interfaceInfoId);

    boolean isNumOfUse(Long id, Long userId);

    boolean saveUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo);

    boolean updateUserInterfaceById(UserInterfaceInfo userInterfaceInfo);

    boolean removeUserInterfaceById(UserInterfaceInfo userInterfaceInfo);

    UserInterfaceInfo getUserInterfaceById(long id);
}
