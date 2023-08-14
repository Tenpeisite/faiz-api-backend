package com.zhj.common.service;


public interface InnerUserInterfaceInfoService {
    boolean invokeCount(long interfaceInfoId, long userId);

    /**
     * 判断该接口是否还有剩余次数
     * @param id
     * @param userId
     * @return
     */
    boolean isNumOfUse(Long id, Long userId);
}
