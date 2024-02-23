package com.zhj;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhj.common.constant.RedisConstant;
import com.zhj.common.constant.UserConstant;
import com.zhj.common.model.entity.DailyCheckIn;
import com.zhj.common.model.entity.ProductOrder;
import com.zhj.common.model.enums.PayTypeStatusEnum;
import com.zhj.common.model.enums.PaymentStatusEnum;
import com.zhj.project.MyApplication;
import com.zhj.project.service.DailyCheckInService;
import com.zhj.project.service.OrderService;
import com.zhj.project.service.ProductOrderService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/6/21 10:48
 */
@SpringBootTest(classes = MyApplication.class)
public class Test1 {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void test1() {
        stringRedisTemplate.opsForZSet().add(RedisConstant.BLACK_LIST, "127.0.0.1", System.currentTimeMillis());
        Set<String> list = stringRedisTemplate.opsForZSet().range(RedisConstant.BLACK_LIST, 0, -1);
        list.forEach(System.out::println);
    }

    @Test
    public void test2() {
        String s = DigestUtils.md5DigestAsHex((UserConstant.SALT + "12345678").getBytes());
        System.out.println(s);
    }

    @Test
    public void test3() {
        UUID uuid = UUID.randomUUID();
        String combinedString = UserConstant.SALT + 12 + 1234 + uuid.toString();
        // 使用MD5哈希算法生成Key
        String key = DigestUtil.md5Hex(combinedString);
        System.out.println(key.length());
    }

    @Resource
    private DailyCheckInService dailyCheckInService;

    @Test
    public void testSignIn() {
        //根据日期生成路径   2024-01/01
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        //String today = "2024-02-07";
        Long userId = 1755188564133539842L;
        Long count = dailyCheckInService.lambdaQuery().eq(DailyCheckIn::getUserId, userId)
                .eq(DailyCheckIn::getSignInDate, today)
                .count();
        System.out.println("今天是否签到：" + (count > 0 ? true : false));
    }

    @Test
    public void test(){
        Date date = DateUtil.date(System.currentTimeMillis());
        Date expirationTime = DateUtil.offset(date, DateField.MINUTE, 5);
        System.out.println(expirationTime.toString());
    }

    @Resource
    private ProductOrderService productOrderService;

    @Test
    public void testOrderCount(){
        //LambdaQueryWrapper<ProductOrder> orderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //orderLambdaQueryWrapper.eq(ProductOrder::getUserId, 1755188564133539842L);
        //orderLambdaQueryWrapper.eq(ProductOrder::getProductId, 1695338876708544514L);
        //orderLambdaQueryWrapper.eq(ProductOrder::getStatus, PaymentStatusEnum.NOTPAY.getValue());
        //orderLambdaQueryWrapper.or().eq(ProductOrder::getStatus, PaymentStatusEnum.SUCCESS.getValue());

        LambdaQueryWrapper<ProductOrder> orderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderLambdaQueryWrapper.eq(ProductOrder::getUserId, 1755188564133539842L);
        orderLambdaQueryWrapper.eq(ProductOrder::getProductId, 1695338876708544514L);
        orderLambdaQueryWrapper.and(productOrderLambdaQueryWrapper -> {
            productOrderLambdaQueryWrapper.eq(ProductOrder::getStatus, PaymentStatusEnum.NOTPAY.getValue())
                    .or().eq(ProductOrder::getStatus, PaymentStatusEnum.SUCCESS.getValue());
        });

        long orderCount = productOrderService.count(orderLambdaQueryWrapper);
        System.out.println(orderCount);
    }

    @Resource
    private OrderService orderService;

    @Test
    public void test11(){
        List<ProductOrder> orderList = orderService.getNoPayOrderByDuration(5, false, PayTypeStatusEnum.ALIPAY.getValue());
        System.out.println(orderList);

        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();

        // 加上五分钟
        LocalDateTime fiveMinutesLater = currentTime.plus(5, ChronoUnit.MINUTES);

        System.out.println("当前时间: " + currentTime);
        System.out.println("当前时间: " + new Date());
        System.out.println("当前时间后五分钟: " + fiveMinutesLater);
    }


}
