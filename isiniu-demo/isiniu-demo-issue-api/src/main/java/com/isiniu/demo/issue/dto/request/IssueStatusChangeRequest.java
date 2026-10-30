package com.isiniu.demo.issue.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 变更问题状态。
 *
 * @author isiniu
 */
@Data
@Schema(description = "变更问题状态")
public class IssueStatusChangeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "目标状态不能为空")
    @Schema(description = "目标状态编码，见 IssueStatusEnum.code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetStatus;
}
