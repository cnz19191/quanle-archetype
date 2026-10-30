package com.isiniu.demo.kernel;

/**
 * 本项目已分配的错误码号段。号段规则见 {@code com.isiniu.common.error.ErrorCodeRange}，
 * 新增领域时在这里登记下一段百号。
 *
 * <p>当前分配：鉴权 0100-0199，问题 0200-0299，评论 0300-0399。
 *
 * @author isiniu
 */
public final class ErrorCodeRange {

    /** 鉴权段。 */
    public static final int AUTH_MIN = com.isiniu.common.error.ErrorCodeRange.AUTH_MIN;

    public static final int AUTH_MAX = com.isiniu.common.error.ErrorCodeRange.AUTH_MAX;

    /** 问题领域段。 */
    public static final int ISSUE_MIN = com.isiniu.common.error.ErrorCodeRange.domainMin(0);

    public static final int ISSUE_MAX = com.isiniu.common.error.ErrorCodeRange.domainMax(0);

    /** 评论领域段。 */
    public static final int COMMENT_MIN = com.isiniu.common.error.ErrorCodeRange.domainMin(1);

    public static final int COMMENT_MAX = com.isiniu.common.error.ErrorCodeRange.domainMax(1);

    private ErrorCodeRange() {}
}
