package com.isiniu.common.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.isiniu.common.enums.CommonErrorCode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * 错误码号段约定。
 *
 * @author isiniu
 */
class ErrorCodeRangeTest {

    @Test
    void domainRangesAreAllocatedByHundred() {
        assertEquals(200, ErrorCodeRange.domainMin(0));
        assertEquals(299, ErrorCodeRange.domainMax(0));
        assertEquals(300, ErrorCodeRange.domainMin(1));
        assertEquals(399, ErrorCodeRange.domainMax(1));
    }

    @Test
    void commonCodesStayInsideFrameworkRangeAndAreUnique() {
        Set<String> codes = new HashSet<>();
        Arrays.stream(CommonErrorCode.values()).forEach(code -> {
            assertTrue(code.getCode().startsWith("SYS"));
            int sequence = Integer.parseInt(code.getCode().substring(3));
            assertTrue(sequence >= ErrorCodeRange.FRAMEWORK_MIN && sequence <= ErrorCodeRange.FRAMEWORK_MAX);
            assertTrue(codes.add(code.getCode()), "重复错误码: " + code.getCode());
        });
    }
}
