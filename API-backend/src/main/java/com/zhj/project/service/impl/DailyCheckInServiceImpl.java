package com.zhj.project.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhj.common.model.entity.DailyCheckIn;
import com.zhj.common.model.entity.User;
import com.zhj.common.model.vo.UserVO;
import com.zhj.common.utils.ErrorCode;
import com.zhj.project.exception.BusinessException;
import com.zhj.project.mapper.DailyCheckInMapper;
import com.zhj.project.service.DailyCheckInService;
import com.zhj.project.service.UserService;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: zhj
 * @Date: 2023/08/31 11:47:57
 * @Version: 1.0
 * @Description: 每日签到服务impl
 */
@Service
public class DailyCheckInServiceImpl extends ServiceImpl<DailyCheckInMapper, DailyCheckIn>
        implements DailyCheckInService {

    @Resource
    private UserService userService;

    @Override
    public void signIn(HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();

        //根据日期生成路径   2024-01/01
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        Long count = lambdaQuery().eq(DailyCheckIn::getUserId, userId)
                .eq(DailyCheckIn::getSignInDate, today)
                .count();
        //已经签过到了
        if(count.intValue()>0){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"您今天已经签过到了！");
        }

        //插入数据
        DailyCheckIn dailyCheckIn = new DailyCheckIn();
        dailyCheckIn.setUserId(userId);
        dailyCheckIn.setDescription("签到");
        dailyCheckIn.setAddPoints(10);
        dailyCheckIn.setSignInDate(today);

        //更新数据
        boolean flag1 = save(dailyCheckIn);
        boolean flag2 = userService.lambdaUpdate().setSql("balance = balance + 10").eq(User::getId, userId).update();
        if(!(flag1&&flag2)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"签到失败，请稍后重试！");
        }
    }
}




