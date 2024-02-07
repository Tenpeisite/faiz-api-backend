package com.zhj.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhj.common.model.entity.DailyCheckIn;
import com.zhj.common.model.vo.UserVO;
import com.zhj.common.utils.BaseResponse;
import com.zhj.common.utils.ErrorCode;
import com.zhj.common.utils.ResultUtils;
import com.zhj.project.exception.BusinessException;
import com.zhj.project.service.DailyCheckInService;
import com.zhj.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author: zhj
 * @Date: 2023/08/31 11:51:14
 * @Version: 1.0
 * @Description: 签到接口
 */
@RestController
@RequestMapping("/dailyCheckIn")
@Slf4j
public class DailyCheckInController {

    @Resource
    private DailyCheckInService dailyCheckInService;

    @Resource
    private UserService userService;


    // region 增删改查

    /**
     * 签到
     *
     * @param request 请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PostMapping("/doCheckIn")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> doDailyCheckIn(HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        LambdaQueryWrapper<DailyCheckIn> dailyCheckInLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dailyCheckInLambdaQueryWrapper.eq(DailyCheckIn::getUserId, loginUser.getId());
        DailyCheckIn dailyCheckIn = dailyCheckInService.getOne(dailyCheckInLambdaQueryWrapper);
        if (ObjectUtils.isNotEmpty(dailyCheckIn)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "签到失败,今日已签到");
        }
        dailyCheckIn = new DailyCheckIn();
        dailyCheckIn.setUserId(loginUser.getId());
        dailyCheckIn.setAddPoints(10);
        boolean dailyCheckInResult = dailyCheckInService.save(dailyCheckIn);
        boolean addWalletBalance = userService.addWalletBalance(loginUser.getId(), dailyCheckIn.getAddPoints());
        boolean result = dailyCheckInResult & addWalletBalance;
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(true);
    }
}
