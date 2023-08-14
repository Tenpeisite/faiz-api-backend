package com.zhj.common.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author zhj
 * @version 1.0
 * @date 2023/7/12 20:57
 */
@Data
@AllArgsConstructor
public class InterfacePageVo {
    List<InterfaceVO> records;
    long total;
}
