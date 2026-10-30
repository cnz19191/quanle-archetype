package com.isiniu.demo;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 从 HTTP 到数据库的标杆链路。数据源是 H2，不依赖真实 MySQL。
 *
 * @author isiniu
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IssueApiTest {

    private static final String PASSWORD = "password";

    @Resource
    private MockMvc mockMvc;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clean() {
        jdbcTemplate.update("DELETE FROM comment_main");
        jdbcTemplate.update("DELETE FROM issue_main");
        jdbcTemplate.update("DELETE FROM sys_refresh_token");
        jdbcTemplate.update("DELETE FROM sys_user");
        insertUser(7L, "tester");
        insertUser(8L, "other");
    }

    @Test
    void createRequiresLogin() throws Exception {
        mockMvc.perform(post("/api/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("打印机无法连接", "BIZ-1")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("SYS0003"));
    }

    @Test
    void createPageAndChangeStatus() throws Exception {
        String token = login("tester");
        MvcResult created = mockMvc.perform(post("/api/issues")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("打印机无法连接", "BIZ-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.reporterId").value("7"))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.createTime", matchesPattern("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andExpect(header().exists("X-Trace-Id"))
                .andReturn();
        String id = JsonPath.read(created.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(post("/api/issues")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("打印机无法连接", "BIZ-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id));

        mockMvc.perform(get("/api/issues")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("title", "打印"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));

        String otherToken = login("other");
        mockMvc.perform(get("/api/issues").header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));

        mockMvc.perform(put("/api/issues/" + id + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetStatus\":\"PROCESSING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ISN0201"));

        mockMvc.perform(put("/api/issues/" + id + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetStatus\":\"PROCESSING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));

        // 跨领域示例：评论领域通过 issue-api 校验问题归属
        mockMvc.perform(post("/api/issues/" + id + "/comments")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"已处理\",\"bizNo\":\"CMT-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.data.issueId").value(id))
                .andExpect(jsonPath("$.data.authorId").value("7"));

        mockMvc.perform(get("/api/issues/" + id + "/comments")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].content").value("已处理"));

        mockMvc.perform(get("/api/portal/issues").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("7"))
                .andExpect(jsonPath("$.data.page.total").value(1));
    }

    @Test
    void rejectsBlankTitle() throws Exception {
        String token = login("tester");
        mockMvc.perform(post("/api/issues")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"bizNo\":\"BIZ-2\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SYS0001"));
    }

    @Test
    void loginRejectsBadPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("tester", "wrong")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ISN0101"));
    }

    @Test
    void refreshRotatesAndRevokesOldToken() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("tester", PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        String refreshToken =
                JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.refreshToken");

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();
        String newAccessToken =
                JsonPath.read(refreshResult.getResponse().getContentAsString(), "$.data.accessToken");

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(newAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("7"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ISN0103"));
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("tester", PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken =
                JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.accessToken");
        String refreshToken =
                JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.refreshToken");

        mockMvc.perform(post("/api/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ISN0103"));
    }

    private String login(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(username, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }

    private void insertUser(Long id, String username) {
        jdbcTemplate.update(
                "INSERT INTO sys_user (id, username, password_hash, display_name, status, create_time, update_time, deleted)"
                        + " VALUES (?, ?, ?, ?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)",
                id,
                username,
                passwordEncoder.encode(PASSWORD),
                username);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String loginJson(String username, String password) {
        return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
    }

    private String json(String title, String bizNo) {
        return "{\"title\":\"" + title + "\",\"content\":\"一楼\",\"bizNo\":\"" + bizNo + "\"}";
    }
}
