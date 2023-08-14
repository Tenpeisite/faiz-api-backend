package com.zhj.common.model.dto.interfaceinfo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class InterfaceInfoInvokeRequest implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 请求参数
     */
    private String requestParams;

    private static final long serialVersionUID = 1L;

}