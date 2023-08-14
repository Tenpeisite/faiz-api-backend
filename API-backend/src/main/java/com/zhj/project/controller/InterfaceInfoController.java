package com.zhj.project.controller;


import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhj.apiclientsdk.client.ApiClient;
import com.zhj.common.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.zhj.common.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import com.zhj.common.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.zhj.common.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.zhj.common.model.entity.InterfaceInfo;
import com.zhj.common.model.entity.User;
import com.zhj.common.model.enums.InterfaceInfoStatusEnum;
import com.zhj.common.model.vo.InterfacePageVo;
import com.zhj.common.model.vo.InterfaceVO;
import com.zhj.common.model.vo.RequestParamsRemarkVO;
import com.zhj.common.model.vo.ResponseParamsRemarkVO;
import com.zhj.common.utils.*;
import com.zhj.project.service.RequestMethod;
import com.zhj.project.annotation.AuthCheck;
import com.zhj.project.exception.BusinessException;
import com.zhj.project.service.InterfaceInfoService;
import com.zhj.project.service.impl.RequestMethodFactory;
import com.zhj.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 帖子接口
 *
 * @author zhj
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private ApiClient apiClient;

    // region 增删改查

    /**
     * 创建
     *
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        // 校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
        //请求参数
        interfaceInfo.setRequestParamsRemark(JSON.toJSONString(interfaceInfoAddRequest.getRequestParamsRemark()));
        //响应参数
        interfaceInfo.setResponseParamsRemark(JSON.toJSONString(interfaceInfoAddRequest.getResponseParamsRemark()));
        boolean result = interfaceInfoService.saveInterface(interfaceInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getInterfaceById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = interfaceInfoService.removeInterfaceById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param interfaceInfoUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest,
                                                     HttpServletRequest request) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        User user = userService.getLoginUser(request);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getInterfaceById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //请求参数
        interfaceInfo.setRequestParamsRemark(JSON.toJSONString(interfaceInfoUpdateRequest.getRequestParamsRemark()));
        //响应参数
        interfaceInfo.setResponseParamsRemark(JSON.toJSONString(interfaceInfoUpdateRequest.getResponseParamsRemark()));
        boolean result = interfaceInfoService.updateInterfaceById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<InterfaceVO> getInterfaceInfoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getInterfaceById(id);
        InterfaceVO interfaceVO = BeanUtil.copyProperties(interfaceInfo, InterfaceVO.class, "requestParamsRemark", "responseParamsRemark");
        interfaceVO.setRequestParamsRemark(JSON.parseObject(interfaceInfo.getRequestParamsRemark(), List.class));
        interfaceVO.setResponseParamsRemark(JSON.parseObject(interfaceInfo.getResponseParamsRemark(), List.class));
        return ResultUtils.success(interfaceVO);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<InterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        if (interfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list(queryWrapper);
        return ResultUtils.success(interfaceInfoList);
    }

    /**
     * 分页获取列表
     *
     * @param interfaceInfoQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<InterfacePageVo> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest, HttpServletRequest request) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        String name = interfaceInfoQuery.getName();
        String method = interfaceInfoQuery.getMethod();
        String description = interfaceInfoQuery.getDescription();
        Integer status = interfaceInfoQuery.getStatus();
        // content 需支持模糊搜索
        interfaceInfoQuery.setDescription(null);
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<InterfaceInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(description), InterfaceInfo::getDescription, description)
                .like(StringUtils.isNotBlank(name), InterfaceInfo::getName, name)
                .like(StringUtils.isNotBlank(method), InterfaceInfo::getMethod, method)
                .like(status != null, InterfaceInfo::getStatus, status)
                .orderByDesc(InterfaceInfo::getUpdateTime);
        //queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
        //        sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size), queryWrapper);
        List<InterfaceInfo> interfaceInfos = interfaceInfoPage.getRecords();
        List<InterfaceVO> records = interfaceInfos.stream().map(interfaceInfo -> {
            InterfaceVO interfaceVO = BeanUtil.copyProperties(interfaceInfo, InterfaceVO.class, "requestParamsRemark", "responseParamsRemark");
            List<RequestParamsRemarkVO> requestParamsRemarkVOS = JSON.parseObject(interfaceInfo.getRequestParamsRemark(),
                    new com.alibaba.fastjson.TypeReference<List<RequestParamsRemarkVO>>() {
                    });
            interfaceVO.setRequestParamsRemark(requestParamsRemarkVOS);
            List<ResponseParamsRemarkVO> responseParamsRemarkVOS = JSON.parseObject(interfaceInfo.getResponseParamsRemark(),
                    new com.alibaba.fastjson.TypeReference<List<ResponseParamsRemarkVO>>() {
                    });
            interfaceVO.setResponseParamsRemark(responseParamsRemarkVOS);
            return interfaceVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(new InterfacePageVo(records, interfaceInfoPage.getTotal()));
    }

    /**
     * 上线
     *
     * @param idRequest
     * @return
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = idRequest.getId();
        //判断接口是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getInterfaceById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //判断该接口是否可以调用
        User user = new User();
        user.setUserName("zhj");
        //String username = yuApiClient.(user);
        //if (StringUtils.isBlank(username)) {
        //    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口验证失败");
        //}
        //仅本人或管理员可修改
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());
        boolean result = interfaceInfoService.updateInterfaceById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * 下线
     *
     * @param idRequest
     * @return
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = idRequest.getId();
        //判断接口是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getInterfaceById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //仅本人或管理员可修改
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        boolean result = interfaceInfoService.updateInterfaceById(interfaceInfo);
        return ResultUtils.success(result);
    }


    @PostMapping("/invoke")
    public BaseResponse<Object> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,
                                                    HttpServletRequest request) {
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //接口id
        Long id = interfaceInfoInvokeRequest.getId();

        //判断是否存在
        InterfaceInfo oldInterface = interfaceInfoService.getInterfaceById(id);
        if (oldInterface == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (oldInterface.getStatus() == InterfaceInfoStatusEnum.OFFLINE.getValue()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口已关闭");
        }


        //方法名
        String methodName = oldInterface.getMethodName();
        //请求方式
        String methodType = oldInterface.getMethod();
        //参数
        String requestParams = interfaceInfoInvokeRequest.getRequestParams();

        User loginUser = userService.getLoginUser(request);
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();
        ApiClient tempClient = new ApiClient(accessKey, secretKey);
        //client类对象
        Class<? extends ApiClient> clazz = tempClient.getClass();

        Method method = null;
        Object result = null;

        //判断请求方式
        // 简单工厂模式 + 策略模式
        try {
            RequestMethod requestMethod = new RequestMethodFactory().getBean(methodType);
             result = requestMethod.invoke(tempClient, methodName,requestParams);

        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(result);
    }

}
