package com.zhj.project.jobHandler;


import com.zhj.common.model.entity.ProductOrder;
import com.zhj.common.model.enums.PayTypeStatusEnum;
import com.zhj.project.service.OrderService;
import com.zhj.project.service.ProductOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: zhj
 * @Date: 2023年08月24日 09:24
 * @Version: 1.0
 * @Description:
 */
@Slf4j
@Component
public class PayJob {
    @Resource
    private ProductOrderService productOrderService;
    @Resource
    private OrderService orderService;


    /**
     * 微信订单确认
     * 每25s查询一次超过5分钟过期的订单,并且未支付
     */
    @Scheduled(cron = "0/25 * * * * ?")
    public void wxOrderConfirm() {
            List<ProductOrder> orderList = orderService.getNoPayOrderByDuration(5, false, PayTypeStatusEnum.WX.getValue());
            ProductOrderService productOrderService = orderService.getProductOrderServiceByPayType(PayTypeStatusEnum.WX.getValue());
            for (ProductOrder productOrder : orderList) {
                String orderNo = productOrder.getOrderNo();
                try {
                    productOrderService.processingTimedOutOrders(productOrder);
                } catch (Exception e) {
                    log.error("微信超时订单,{},确认异常：{}", orderNo, e.getMessage());
                    break;
                }
            }
    }

    /**
     * 支付宝订单确认
     * 每20s查询一次超过5分钟过期的订单,并且未支付
     */
    @Scheduled(cron = "0/20 * * * * ?")
    public void aliPayOrderConfirm() {
            List<ProductOrder> orderList = orderService.getNoPayOrderByDuration(5, false, PayTypeStatusEnum.ALIPAY.getValue());
            ProductOrderService productOrderService = orderService.getProductOrderServiceByPayType(PayTypeStatusEnum.ALIPAY.getValue());
            for (ProductOrder productOrder : orderList) {
                String orderNo = productOrder.getOrderNo();
                try {
                    productOrderService.processingTimedOutOrders(productOrder);
                } catch (Exception e) {
                    log.error("支付宝超时订单,{},确认异常：{}", orderNo, e.getMessage());
                    break;
                }
            }
    }

    /**
     * 订单确认
     * 每2点删除一次15天前的订单,并且未支付，并且已关闭的订单
     */
    @Scheduled(cron = "* * 2 * * ?")
    public void clearOverdueOrders() {
            List<ProductOrder> orderList = orderService.getNoPayOrderByDuration(15 * 24 * 60, true, "");
            boolean removeResult = productOrderService.removeBatchByIds(orderList);
            if (removeResult) {
                log.info("清除成功");
            }
    }
}
