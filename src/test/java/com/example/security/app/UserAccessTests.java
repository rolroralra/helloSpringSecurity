package com.example.security.app;

import com.example.security.domain.SecurityMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserAccessTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserDetails testUser() {
        return User.builder()
                .username("test")
                .password(passwordEncoder.encode("1234"))
                .roles("TEST")
                .build();
    }

    // https://www.youtube.com/watch?v=MNEgiFeUy_U
    @Order(1)
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

    @Order(2)
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

    @Order(3)
    @DisplayName("3. test-user가 user 페이지를 접근할 수 없다.")
    @Test
    void test_no_auth_cannot_access_user_page() throws Exception {
        mockMvc.perform(get("/user").with(user(testUser())))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/admin").with(user(testUser())))
                .andExpect(status().is4xxClientError());
    }

    @Order(4)
    @DisplayName("4. user가 admin 페이지를 접근할 수 없다.")
    @Test
    @WithMockUser(username = "user")
    void test_user_cannot_access_admin_page() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is4xxClientError());
    }

}
