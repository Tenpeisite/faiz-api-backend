package com.zhj.common.constant;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/4/25 18:40
 */
public interface MqConstant {

    //交换机
    String EXCHANGE_DIRECT_INTERFACE = "exchange.direct.interface";
    //接口调用次数队列
    String INTERFACE_QUEUE = "interface.queue";
    //接口调用次数路由key
    String ROUTERING_KEY_INTERFACE = "changeCount";

    //排行榜队列
    String RANK_LIST_QUEUE="interfaceRank.queue";
    //旁行榜路由key
    String ROUTERING_KEY_RANK = "rankList";
}
