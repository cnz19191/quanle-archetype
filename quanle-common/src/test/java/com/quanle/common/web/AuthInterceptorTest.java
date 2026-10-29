package com.quanle.common.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.quanle.common.enums.CommonErrorCode;
import com.quanle.common.exception.CustomException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

/**
 * 登录占位拦截器。
 *
 * @author quanle
 */
class AuthInterceptorTest {

    @AfterEach
    void tearDown() {
        CurrentUserHolder.clear();
    }

    @Test
    void rejectsMissingUser() throws Exception {
        AuthInterceptor interceptor = new AuthInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/issues");
        HandlerMethod handler = handler("secured");

        CustomException ex = assertThrows(
                CustomException.class, () -> interceptor.preHandle(request, new MockHttpServletResponse(), handler));
        assertEquals(CommonErrorCode.UNAUTHORIZED.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void acceptsUserAndClearsAfterCompletion() throws Exception {
        AuthInterceptor interceptor = new AuthInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/issues");
        request.addHeader(AuthInterceptor.USER_HEADER, "7");
        HandlerMethod handler = handler("secured");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), handler));
        assertEquals(7L, CurrentUserHolder.getUserId());
        interceptor.afterCompletion(request, new MockHttpServletResponse(), handler, null);
        assertNull(CurrentUserHolder.getUserId());
    }

    @Test
    void anonymousMethodSkipsLogin() throws Exception {
        AuthInterceptor interceptor = new AuthInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
        HandlerMethod handler = handler("open");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), handler));
        assertNull(CurrentUserHolder.getUserId());
    }

    private HandlerMethod handler(String method) throws NoSuchMethodException {
        Dummy dummy = new Dummy();
        return new HandlerMethod(dummy, Dummy.class.getDeclaredMethod(method));
    }

    static class Dummy {

        public void secured() {}

        @Anonymous
        public void open() {}
    }
}
