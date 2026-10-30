package com.isiniu.demo.auth.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 刷新令牌表 {@code sys_refresh_token}，只存哈希。
 *
 * @author isiniu
 */
@Data
@TableName("sys_refresh_token")
public class RefreshTokenModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 已吊销。 */
    public static final int REVOKED = 1;

    /** 未吊销。 */
    public static final int NOT_REVOKED = 0;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String tokenHash;

    private LocalDateTime expiresAt;

    private Integer revoked;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
