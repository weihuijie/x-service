package com.x.auth.service.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JwtUtilTest {

    private JwtUtil jwtUtil = new JwtUtil();

    @Test
    public void testGenerateAndValidateToken() {
        String username = "testUser";

        // 生成token
        String token = jwtUtil.generateToken(username,"role");
        assertNotNull(token);
        System.out.println("Generated token: " + token);

        // 验证token
        Boolean isValid = jwtUtil.validateToken(token, username);
        assertTrue(isValid);

        // 提取用户名
        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    public void testExtractUsername() {
        String username = "testUser";
        String token = jwtUtil.generateToken(username,"role");

        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals(username, extractedUsername);
    }
}
