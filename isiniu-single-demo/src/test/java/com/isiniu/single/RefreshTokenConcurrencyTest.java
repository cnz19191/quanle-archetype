package com.isiniu.single;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jayway.jsonpath.JsonPath;
import jakarta.annotation.Resource;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 同一个 refresh token 被并发使用时，只能换到一次新令牌。
 *
 * @author isiniu
 */
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RefreshTokenConcurrencyTest {

    private static final int CONCURRENCY = 8;

    private static final int ROUNDS = 5;

    @LocalServerPort
    private int port;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private PasswordEncoder passwordEncoder;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM sys_refresh_token");
        jdbcTemplate.update("DELETE FROM sys_user WHERE id = 901");
        jdbcTemplate.update(
                "INSERT INTO sys_user (id, username, password_hash, display_name, status, create_time, update_time, deleted)"
                        + " VALUES (901, 'race', ?, 'race', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)",
                passwordEncoder.encode("password"));
    }

    @Test
    void refreshTokenCanOnlyBeRotatedOnce() throws Exception {
        for (int round = 0; round < ROUNDS; round++) {
            String login = post("/api/auth/login", "{\"username\":\"race\",\"password\":\"password\"}");
            String refreshToken = JsonPath.read(login, "$.data.refreshToken");
            CyclicBarrier barrier = new CyclicBarrier(CONCURRENCY);
            ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
            try {
                List<Future<String>> results = new ArrayList<>();
                for (int i = 0; i < CONCURRENCY; i++) {
                    results.add(executor.submit(() -> {
                        barrier.await();
                        return post("/api/auth/refresh", "{\"refreshToken\":\"" + refreshToken + "\"}");
                    }));
                }
                int succeeded = 0;
                for (Future<String> result : results) {
                    if ("0".equals(JsonPath.read(result.get(), "$.code"))) {
                        succeeded++;
                    }
                }
                assertEquals(1, succeeded, "同一个 refresh token 只能成功刷新一次");
            } finally {
                executor.shutdownNow();
            }
        }
    }

    private String post(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }
}
