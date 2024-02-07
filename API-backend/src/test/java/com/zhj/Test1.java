package com.zhj;

import cn.hutool.crypto.digest.DigestUtil;
import com.zhj.common.constant.RedisConstant;
import com.zhj.common.constant.UserConstant;
import com.zhj.project.MyApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.Set;
import java.util.UUID;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/6/21 10:48
 */
//@SpringBootTest(classes = MyApplication.class)
public class Test1 {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void test1(){
        stringRedisTemplate.opsForZSet().add(RedisConstant.BLACK_LIST,"127.0.0.1",System.currentTimeMillis());
        Set<String> list = stringRedisTemplate.opsForZSet().range(RedisConstant.BLACK_LIST, 0, -1);
        list.forEach(System.out::println);
    }

    @Test
    public void test2(){
        String s = DigestUtils.md5DigestAsHex((UserConstant.SALT + "12345678").getBytes());
        System.out.println(s);
    }

    @Test
    public void test3(){
        UUID uuid = UUID.randomUUID();
        String combinedString = UserConstant.SALT + 12 + 1234 + uuid.toString();
        // 使用MD5哈希算法生成Key
        String key = DigestUtil.md5Hex(combinedString);
        System.out.println(key.length());
    }
}
