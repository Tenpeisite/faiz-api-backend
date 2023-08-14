package com.zhj.project.jobHandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.zhj.common.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description 定时任务，每隔一段时间查看黑名单是否有用户需要移除
 * @date 2023/6/21 9:27
 */
@Component
@Slf4j
public class RemoveBlackListJob {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @XxlJob(value = "removeBlackHandler", init = "init", destroy = "destroy")
    public void removeBlackHandler() throws Exception {
        long current = System.currentTimeMillis();
        Set<String> list = stringRedisTemplate.opsForZSet().range(RedisConstant.BLACK_LIST, 0, -1);
        list.forEach(black -> {
            Double score = stringRedisTemplate.opsForZSet().score(RedisConstant.BLACK_LIST, black);
            if (Double.compare(score + RedisConstant.BLACK_EXPIRE_TIME, current) <= 0) {
                //封禁到期
                stringRedisTemplate.opsForZSet().remove(RedisConstant.BLACK_LIST, black);
            }
        });
    }

    public void init() {
        log.info("任务初始化");
    }

    public void destroy() {
        log.info("任务销毁");
    }

}
