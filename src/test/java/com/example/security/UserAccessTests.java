package com.example.security;

import com.example.security.domain.SecurityMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class UserAccessTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    // https://www.youtube.com/watch?v=MNEgiFeUy_U
    @DisplayName("1. user가 user 페이지를 접근할 수 있다.")
    @Test
    @WithMockUser(username = "rolroralra")
    void test_user_can_access_user_page() throws Exception {
        String response = mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        SecurityMessage securityMessage = objectMapper.readValue(response, SecurityMessage.class);
        assertThat(securityMessage.getMessage()).isEqualTo("user page");
    }

    @DisplayName("2. admin이 admin 페이지를 접근할 수 있다.")
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void test_admin_can_access_admin_page() throws Exception {
        String response = mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        SecurityMessage securityMessage = objectMapper.readValue(response, SecurityMessage.class);
        assertThat(securityMessage.getMessage()).isEqualTo("admin page");
    }

}
