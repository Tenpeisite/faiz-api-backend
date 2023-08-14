package com.zhj.common.constant;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/4/25 15:00
 */
public interface RedisConstant {

    String INTERFACE_LEFTCOUNT = "interface:left:";
    Long EXPIRE_TIME=30L;
    String INTERFACE="interface:";
    String BLACK_LIST="user:black";
    Long BLACK_EXPIRE_TIME=30 * 60 * 1000L;
}
