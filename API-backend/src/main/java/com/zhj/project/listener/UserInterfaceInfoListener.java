package com.zhj.project.listener;

import com.zhj.project.service.InterfaceInfoService;
import com.zhj.project.service.UserInterfaceInfoService;
import com.zhj.project.service.UserService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.zhj.common.constant.MqConstant.*;


/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/4/25 18:55
 */
@Component
public class UserInterfaceInfoListener {

    @Autowired
    private UserInterfaceInfoService userInterfaceInfoService;

    @Autowired
    private InterfaceInfoService interfaceInfoService;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = INTERFACE_QUEUE),
            exchange = @Exchange(name = EXCHANGE_DIRECT_INTERFACE, type = ExchangeTypes.DIRECT),
            key = ROUTERING_KEY_INTERFACE
    ))
    public void listenInterfaceQueue(Map<String, Long> map) {
        //将用户对应的接口总调用次数+1，剩余次数-1
        Long userId = map.get("userId");
        Long interfaceInfoId = map.get("interfaceInfoId");
        //userInterfaceInfoService.changeCount(userId,interfaceInfoId);
        //更新用户余额和接口调用次数
        interfaceInfoService.changeCount(userId,interfaceInfoId);
        //更新排行榜
        //userInterfaceInfoService.updateInterfaceRankList(userId,interfaceInfoId);
    }
}
