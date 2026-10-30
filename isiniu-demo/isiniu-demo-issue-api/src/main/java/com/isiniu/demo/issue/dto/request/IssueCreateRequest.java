package com.isiniu.demo.issue.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 新建问题。提交人取当前登录用户，不接收客户端传入的 reporterId。
 *
 * @author isiniu
 */
@Data
@Schema(description = "新建问题")
public class IssueCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "标题不能为空")
    @Size(max = 128, message = "标题不能超过 128 个字符")
    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Size(max = 2000, message = "内容不能超过 2000 个字符")
    @Schema(description = "内容")
    private String content;

    @NotBlank(message = "业务幂等号不能为空")
    @Size(max = 64, message = "业务幂等号不能超过 64 个字符")
    @Schema(description = "业务幂等号，重复提交相同标题时返回已有问题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bizNo;
}
