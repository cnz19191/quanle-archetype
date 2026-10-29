package com.quanle.demo.issue.dto.request;

import com.quanle.common.api.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 问题分页查询。只返回当前登录人的数据。
 *
 * @author quanle
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "问题分页查询")
public class IssuePageRequest extends PageRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "标题，模糊匹配")
    private String title;

    @Schema(description = "状态编码，见 IssueStatusEnum.code")
    private String status;
}
