package com.isiniu.demo.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 评论返回对象。ID 和日期都是字符串。
 *
 * @author isiniu
 */
@Data
@Schema(description = "评论")
public class CommentResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "评论 ID")
    private String id;

    @Schema(description = "问题 ID")
    private String issueId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "作者 ID")
    private String authorId;

    @Schema(description = "创建时间，格式 yyyy-MM-dd HH:mm:ss")
    private String createTime;
}
