package com.isiniu.single;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.isiniu.common.auth.JwtAuthenticationFilter;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterRegistration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;

/**
 * JWT 过滤器只挂在 Spring Security 链里，不被 Servlet 容器再注册一次。
 *
 * @author isiniu
 */
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwtFilterRegistrationTest {

    @Resource
    private ServletWebServerApplicationContext context;

    @Test
    void jwtFilterIsNotRegisteredAsServletFilter() {
        Map<String, ? extends FilterRegistration> filters =
                context.getServletContext().getFilterRegistrations();
        List<String> filterClasses =
                filters.values().stream().map(FilterRegistration::getClassName).toList();

        assertTrue(
                filters.containsKey("springSecurityFilterChain"),
                filters.keySet().toString());
        assertFalse(filterClasses.contains(JwtAuthenticationFilter.class.getName()), filterClasses.toString());
    }
}
