package com.isiniu.single.controller;

import com.isiniu.common.api.CommonResult;
import com.isiniu.common.web.Anonymous;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查，匿名可访问。
 *
 * @author isiniu
 */
@Anonymous
@RestController
@RequestMapping("/api/health")
@Tag(name = "健康检查")
public class HealthController {

    @GetMapping
    @Operation(summary = "健康检查")
    public CommonResult<String> health() {
        return CommonResult.buildSuccessResult("ok");
    }
}
