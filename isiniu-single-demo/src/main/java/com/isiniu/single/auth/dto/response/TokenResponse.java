package com.isiniu.single.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 登录或刷新后返回的令牌对。
 *
 * @author isiniu
 */
@Data
@Schema(description = "令牌")
public class TokenResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "访问令牌，放在 Authorization: Bearer 后面")
    private String accessToken;

    @Schema(description = "刷新令牌，只返回一次，请安全保存")
    private String refreshToken;

    @Schema(description = "令牌类型，固定 Bearer")
    private String tokenType;

    @Schema(description = "访问令牌有效期，单位秒")
    private Long expiresIn;
}
