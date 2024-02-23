package com.zhj.apiinterface.aop;


import com.zhj.apiinterface.exception.BusinessException;
import com.zhj.common.utils.ErrorCode;
import com.zhj.common.utils.RateLimiterUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 朱焕杰
 * @version 1.0
 * @description TODO
 * @date 2023/4/12 15:39
 */
@Aspect
@Component
@Slf4j
public class CheckInterceptor {

    @Pointcut("execution(* com.zhj.apiinterface.controller..*.*(..))")
    public void pt() {
    }

    @Around("pt()")
    public Object invoke(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String apiHeader = request.getHeader("apiHeader");
        if (!"api".equals(apiHeader)) {
            throw new BusinessException(ErrorCode.INTERFACE_USE_ERROR);
        }
        boolean acquire = RateLimiterUtil.acquire();
        if (!acquire) {
            throw new BusinessException(ErrorCode.INTERFACE_USE_FREQUENTLY);
        }
        try {
            return pjp.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.INTERFACE_ERROR);
        }
    }
}
