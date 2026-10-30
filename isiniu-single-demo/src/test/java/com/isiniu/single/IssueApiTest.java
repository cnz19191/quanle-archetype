package com.isiniu.single;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 单模块从 HTTP 到数据库的标杆链路。数据源是 H2。
 *
 * @author isiniu
 */
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
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
        jdbcTemplate.update("DELETE FROM issue_main");
        jdbcTemplate.update("DELETE FROM sys_refresh_token");
        jdbcTemplate.update("DELETE FROM sys_user");
        jdbcTemplate.update(
                "INSERT INTO sys_user (id, username, password_hash, display_name, status, create_time, update_time, deleted)"
                        + " VALUES (7, 'tester', ?, 'tester', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)",
                passwordEncoder.encode(PASSWORD));
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
        String token = login();
        MvcResult created = mockMvc.perform(post("/api/issues")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("打印机无法连接", "BIZ-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
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

        mockMvc.perform(put("/api/issues/" + id + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetStatus\":\"PROCESSING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PROCESSING"));

        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("7"));
    }

    @Test
    void rejectsBlankTitle() throws Exception {
        String token = login();
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
                        .content("{\"username\":\"tester\",\"password\":\"wrong\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ISN0101"));
    }

    private String login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"tester\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String json(String title, String bizNo) {
        return "{\"title\":\"" + title + "\",\"content\":\"一楼\",\"bizNo\":\"" + bizNo + "\"}";
    }
}
