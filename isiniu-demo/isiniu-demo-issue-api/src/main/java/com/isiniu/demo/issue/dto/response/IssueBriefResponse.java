package com.isiniu.demo.issue.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 问题摘要，供其他领域使用，只包含校验归属需要的字段。
 *
 * @author isiniu
 */
@Data
@Schema(description = "问题摘要")
public class IssueBriefResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "问题 ID")
    private String id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "状态编码")
    private String status;

    @Schema(description = "提交人 ID")
    private String reporterId;
}
