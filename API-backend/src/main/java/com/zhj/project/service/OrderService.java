package com.zhj.project.service;


import com.zhj.common.model.entity.ProductOrder;
import com.zhj.common.model.vo.ProductOrderVo;
import com.zhj.common.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Author: zhj
 * @Date: 2023年08月25日 22:22
 * @Version: 1.0
 * @Description:
 */
public interface OrderService {
    /**
     * 处理订单通知
     *
     * @param notifyData 通知数据
     * @param request    要求
     * @return {@link String}
     */
    String doOrderNotify(String notifyData, HttpServletRequest request);

    /**
     * 按付费类型获取产品订单服务
     *
     * @param payType 付款类型
     * @return {@link ProductOrderService}
     */
    ProductOrderService getProductOrderServiceByPayType(String payType);

    /**
     * 按付款类型创建订单
     *
     * @param productId 产品id
     * @param payType   付款类型
     * @param loginUser 登录用户
     * @return {@link ProductOrderVo}
     */
    ProductOrderVo createOrderByPayType(Long productId, String payType, UserVO loginUser);

}
