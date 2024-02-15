package com.zhj.project.jobHandler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhj.common.model.entity.DailyCheckIn;
import com.zhj.project.service.DailyCheckInService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: zhj
 * @Date: 2023/08/31 12:10:58
 * @Version: 1.0
 * @Description: 签到任务
 */
@Component
public class DailyCheckInJob {
    @Resource
    private DailyCheckInService dailyCheckInService;

    /**
     * 每天晚上12点批量清除签到列表
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void clearCheckInList() {
        // 每批删除的数据量
        int batchSize = 1000;
        // 是否还有数据需要删除
        boolean hasMoreData = true;
        while (hasMoreData) {
            // 分批查询数据
            List<DailyCheckIn> dataList = dailyCheckInService.list(new QueryWrapper<DailyCheckIn>().last("LIMIT " + batchSize));

            if (dataList.isEmpty()) {
                // 没有数据了，退出循环
                hasMoreData = false;
            } else {
                // 批量删除数据
                dailyCheckInService.removeByIds(dataList.stream().map(DailyCheckIn::getId).collect(Collectors.toList()));
            }
        }

    }
}
