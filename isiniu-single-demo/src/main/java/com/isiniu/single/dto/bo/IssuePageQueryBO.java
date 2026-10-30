package com.isiniu.single.dto.bo;

import lombok.Data;

/**
 * 分页查询条件。状态和提交人由 Service 转成落库值后写入。
 *
 * @author isiniu
 */
@Data
public class IssuePageQueryBO {

    private Integer pageNum;

    private Integer pageSize;

    private String title;

    private Integer status;

    private Long reporterId;
}
