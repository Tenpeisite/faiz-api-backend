package com.zhj.project.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhj.common.model.entity.ProductInfo;
import com.zhj.common.model.entity.ProductOrder;
import com.zhj.common.model.enums.PaymentStatusEnum;
import com.zhj.common.model.vo.ProductOrderVo;
import com.zhj.common.model.vo.UserVO;
import com.zhj.project.mapper.ProductOrderMapper;
import org.springframework.beans.BeanUtils;

/**
 * @author zhj
 * @version 1.0
 * @description
 * @date 2024/2/16 13:19
 */
public abstract class AbstractProductOrderService extends ServiceImpl<ProductOrderMapper, ProductOrder> implements ProductOrderService{

    @Override
    public ProductOrderVo getProductOrder(Long productId, UserVO loginUser, String payType) {
        LambdaQueryWrapper<ProductOrder> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ProductOrder::getProductId, productId);
        lambdaQueryWrapper.eq(ProductOrder::getStatus, PaymentStatusEnum.NOTPAY.getValue());
        lambdaQueryWrapper.eq(ProductOrder::getPayType, payType);
        lambdaQueryWrapper.eq(ProductOrder::getUserId, loginUser.getId());
        lambdaQueryWrapper.gt(ProductOrder::getExpirationTime, DateUtil.date(System.currentTimeMillis()));
        lambdaQueryWrapper.orderByDesc(ProductOrder::getCreateTime);
        lambdaQueryWrapper.last("LIMIT 1");

        ProductOrder oldOrder = this.getOne(lambdaQueryWrapper);
        if (oldOrder == null) {
            return null;
        }
        ProductOrderVo productOrderVo = new ProductOrderVo();
        BeanUtils.copyProperties(oldOrder, productOrderVo);
        productOrderVo.setTotal(oldOrder.getTotal().toString());
        productOrderVo.setProductInfo(JSONUtil.toBean(oldOrder.getProductInfo(), ProductInfo.class));
        return productOrderVo;
    }
}
