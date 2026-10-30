package com.isiniu.demo.issue.dto.bo;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 问题查询结果。Mapper 返回 BO，不返回 Response。
 *
 * @author isiniu
 */
@Data
public class IssueBO {

    private Long id;

    private String title;

    private String content;

    private Integer status;

    private Long reporterId;

    private String bizNo;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
