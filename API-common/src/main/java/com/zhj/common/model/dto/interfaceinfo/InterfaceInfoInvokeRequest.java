package com.zhj.common.model.dto.interfaceinfo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.Serializable;

/**
 * 更新请求
 *
 * @author zhj
 * 
 */
@Data
public class InterfaceInfoInvokeRequest implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 请求参数
     */
    //private String requestParams;

    private JSONObject requestParams;

    private static final long serialVersionUID = 1L;

}