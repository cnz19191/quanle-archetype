package com.isiniu.demo.issue.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 问题返回对象。ID 和日期都是字符串。
 *
 * @author isiniu
 */
@Data
@Schema(description = "问题")
public class IssueResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "问题 ID")
    private String id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "状态编码")
    private String status;

    @Schema(description = "状态说明")
    private String statusDesc;

    @Schema(description = "提交人 ID")
    private String reporterId;

    @Schema(description = "业务幂等号")
    private String bizNo;

    @Schema(description = "创建时间，格式 yyyy-MM-dd HH:mm:ss")
    private String createTime;

    @Schema(description = "更新时间，格式 yyyy-MM-dd HH:mm:ss")
    private String updateTime;
}
