package com.isiniu.demo.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * 当前登录用户。
 *
 * @author isiniu
 */
@Data
@Schema(description = "当前用户")
public class LoginUserResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户 ID")
    private String id;

    @Schema(description = "登录名")
    private String username;

    @Schema(description = "显示名")
    private String displayName;

    @Schema(description = "角色")
    private List<String> roles;
}
