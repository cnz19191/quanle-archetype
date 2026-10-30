package com.isiniu.demo.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 新建评论。作者取当前登录用户。
 *
 * @author isiniu
 */
@Data
@Schema(description = "新建评论")
public class CommentCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过 1000 个字符")
    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @NotBlank(message = "业务幂等号不能为空")
    @Size(max = 64, message = "业务幂等号不能超过 64 个字符")
    @Schema(description = "业务幂等号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bizNo;
}
