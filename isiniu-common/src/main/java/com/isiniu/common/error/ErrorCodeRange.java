package com.isiniu.common.error;

/**
 * 错误码号段约定。错误码格式为「模块前缀 + 4 位序号」，序号必须落在对应号段内。
 *
 * <p>框架公共错误码固定使用 {@code SYS} 前缀，占用 0001-0099；鉴权占用 0100-0199；
 * 业务领域从 0200 起，每个领域 100 个号，按顺序申请。各项目的 kernel 模块登记本项目已分配的领域。
 *
 * @author isiniu
 */
public final class ErrorCodeRange {

    /** 框架公共段（前缀 SYS）最小值。 */
    public static final int FRAMEWORK_MIN = 1;

    /** 框架公共段最大值。 */
    public static final int FRAMEWORK_MAX = 99;

    /** 鉴权段最小值。 */
    public static final int AUTH_MIN = 100;

    /** 鉴权段最大值。 */
    public static final int AUTH_MAX = 199;

    /** 业务领域段起点。 */
    public static final int BUSINESS_MIN = 200;

    /** 业务领域段终点。 */
    public static final int BUSINESS_MAX = 9999;

    /** 每个业务领域占用的号段大小。 */
    public static final int DOMAIN_BLOCK_SIZE = 100;

    private ErrorCodeRange() {}

    /**
     * 计算第 n 个业务领域（从 0 开始）的号段下界。
     *
     * @param index 领域序号，从 0 开始
     * @return 号段下界
     */
    public static int domainMin(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("领域序号不能为负数");
        }
        return BUSINESS_MIN + index * DOMAIN_BLOCK_SIZE;
    }

    /**
     * 计算第 n 个业务领域（从 0 开始）的号段上界。
     *
     * @param index 领域序号，从 0 开始
     * @return 号段上界
     */
    public static int domainMax(int index) {
        return domainMin(index) + DOMAIN_BLOCK_SIZE - 1;
    }
}
