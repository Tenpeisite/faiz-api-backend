package com.zhj.project.service.impl.inner;


import com.zhj.project.mapper.UsernameMapper;
import com.zhj.common.service.InnerUsernameService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 鏈辩剷鏉�
 * @description 针对表【username】的数据库操作Service实现
 * @createDate 2023-04-12 14:15:05
 */
@DubboService
public class InnerUsernameServiceImpl implements InnerUsernameService {

    @Autowired
    private UsernameMapper usernameMapper;

    @Override
    public String getRandomName() {
        return usernameMapper.getRandomName();
    }
}
