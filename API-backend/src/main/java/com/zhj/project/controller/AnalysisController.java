package com.zhj.project.controller;

import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.zhj.common.model.entity.InterfaceInfo;
import com.zhj.common.model.entity.UserInterfaceInfo;
import com.zhj.common.model.vo.InterfaceInfoVO;
import com.zhj.common.utils.BaseResponse;
import com.zhj.common.utils.ResultUtils;
import com.zhj.project.mapper.UserInterfaceInfoMapper;
import com.zhj.project.service.InterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.zhj.common.constant.CommonConstant.INTERFACE_RANK;


/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/4/11 21:08
 */
@RestController
@RequestMapping("/analysis")
@Slf4j
public class AnalysisController {

    @Autowired
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Autowired
    private InterfaceInfoService interfaceInfoService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private Cache<String, String> stringCache;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    @GetMapping("/top/interface/invoke")
    public BaseResponse<List<InterfaceInfoVO>> listTopInvokeInterfaceInfo() {
        //从redis中查询排名前三
        //Set<String> range = stringRedisTemplate.opsForZSet().reverseRange(INTERFACE_RANK, 0, 2);
        // 按照 score 值倒序获取排名前 3 的元素及其 score 值
        Set<ZSetOperations.TypedTuple<String>> set = stringRedisTemplate.opsForZSet().reverseRangeWithScores(INTERFACE_RANK, 0, 2);
        //判断reids中是否有数据
        if (!CollectionUtils.isEmpty(set)) {
            //有数据
            List<InterfaceInfoVO> interfaceInfoVOS = set.stream()
                    .map(item -> {
                        InterfaceInfoVO interfaceInfoVO = JSON.parseObject(item.getValue(), InterfaceInfoVO.class);
                        interfaceInfoVO.setTotalNum(item.getScore().intValue());
                        return interfaceInfoVO;
                    })
                    .collect(Collectors.toList());
            return ResultUtils.success(interfaceInfoVOS);
        }
        //查询排名前三的接口
        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoMapper.listTopInvokeInterfaceInfo(3);
        List<InterfaceInfoVO> interfaceInfoVOS = userInterfaceInfoList.stream().map(item -> {
            InterfaceInfo interfaceInfo = interfaceInfoService.getById(item.getInterfaceInfoId());
            InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
            interfaceInfoVO.setTotalNum(item.getTotalNum());
        //stringRedisTemplate.opsForZSet().add(INTERFACE_RANK,JSON.toJSONString(interfaceInfoVO),interfaceInfoVO.getTotalNum());
            return interfaceInfoVO;
        }).collect(Collectors.toList());
        //添加缓存到数据库
        Set<ZSetOperations.TypedTuple<String>> collect = interfaceInfoVOS.stream().map(item -> {
            return ZSetOperations.TypedTuple.of(JSON.toJSONString(item), item.getTotalNum().doubleValue());
        }).collect(Collectors.toSet());
        stringRedisTemplate.opsForZSet().add(INTERFACE_RANK,collect);
        return ResultUtils.success(interfaceInfoVOS);
    }


}
