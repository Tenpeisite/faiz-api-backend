package com.zhj.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhj.common.model.entity.ProductInfo;
import com.zhj.common.model.entity.ProductOrder;
import com.zhj.common.model.entity.RechargeActivity;
import com.zhj.common.model.enums.PayTypeStatusEnum;
import com.zhj.common.model.enums.PaymentStatusEnum;
import com.zhj.common.model.enums.ProductTypeStatusEnum;
import com.zhj.common.model.vo.ProductOrderVo;
import com.zhj.common.model.vo.UserVO;
import com.zhj.common.utils.ErrorCode;
import com.zhj.project.exception.BusinessException;
import com.zhj.project.service.impl.ProductInfoServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author zhj
 * @version 1.0
 * @description
 * @date 2024/2/16 12:52
 */
public abstract class AbstractOrderService implements OrderService {

    @Resource
    protected ProductOrderService productOrderService;

    @Resource
    protected RechargeActivityService rechargeActivityService;

    @Resource
    protected ProductInfoServiceImpl productInfoService;

    @Resource
    protected Map<String,ProductOrderService> payTypeMap;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProductOrderVo createOrderByPayType(Long productId, String payType, UserVO loginUser) {
        // 按付费类型获取产品订单服务Bean
        ProductOrderService productOrderService = payTypeMap.get(payType);
        // 订单存在就返回不再新创建
        ProductOrderVo getProductOrderVo = productOrderService.getProductOrder(productId, loginUser, payType);
        if (getProductOrderVo != null) {
            return getProductOrderVo;
        }
        // 检查是否购买充值活动
        checkBuyRechargeActivity(loginUser.getId(), productId);
        // 保存订单,返回vo信息
        ProductOrderVo productOrderVo = productOrderService.saveProductOrder(productId, loginUser);
        return productOrderVo;
    }


    /**
     * 检查购买充值活动
     *
     * @param userId    用户id
     * @param productId 产品订单id
     */
    protected abstract void checkBuyRechargeActivity(Long userId, Long productId) ;


}
