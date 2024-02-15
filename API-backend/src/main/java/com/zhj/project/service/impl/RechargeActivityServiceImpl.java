package com.zhj.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhj.common.model.entity.RechargeActivity;
import com.zhj.project.mapper.RechargeActivityMapper;
import com.zhj.project.service.RechargeActivityService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: zhj
 * @Date: 2023/09/11 11:26:46
 * @Version: 1.0
 * @Description: 充值活动服务impl
 */
@Service
public class RechargeActivityServiceImpl extends ServiceImpl<RechargeActivityMapper, RechargeActivity>
        implements RechargeActivityService {

    @Override
    public List<RechargeActivity> getRechargeActivityByOrderNo(String orderNo) {
        LambdaQueryWrapper<RechargeActivity> activityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        activityLambdaQueryWrapper.eq(RechargeActivity::getOrderNo, orderNo);
        return this.list(activityLambdaQueryWrapper);
    }
}




