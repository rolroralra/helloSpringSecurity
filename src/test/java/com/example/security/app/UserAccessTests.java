package com.example.security.app;

import com.example.security.domain.SecurityMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
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
    @DisplayName("2. user가 admin 페이지를 접근할 수 없다.")
    @Test
    @WithMockUser(username = "user")
    void test_user_cannot_access_admin_page() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is4xxClientError());
    }

    @Order(3)
    @DisplayName("3. admin이 admin 페이지를 접근할 수 있다.")
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void test_admin_can_access_admin_page() throws Exception {
        String response = mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        SecurityMessage securityMessage = objectMapper.readValue(response, SecurityMessage.class);
        assertThat(securityMessage.getMessage()).isEqualTo("admin page");
    }

    @Order(4)
    @DisplayName("4. admin이 user 페이지를 접근할 수 있다.")
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void test_admin_can_access_user_page() throws Exception {
        String response = mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        SecurityMessage securityMessage = objectMapper.readValue(response, SecurityMessage.class);
        assertThat(securityMessage.getMessage()).isEqualTo("user page");
    }

    @Order(5)
    @DisplayName("5. test-user가 user 페이지를 접근할 수 없다.")
    @Test
    void test_no_auth_cannot_access_user_page() throws Exception {
        mockMvc.perform(get("/user").with(user(testUser())))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/admin").with(user(testUser())))
                .andExpect(status().is4xxClientError());
    }

    @Order(6)
    @DisplayName("6. anonymous-user가 login 페이지를 접근할 수 있다.")
    @Test
    @WithAnonymousUser
    void test_anonymous_user_can_access_login_page() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Order(7)
    @DisplayName("7. index 페이지는 로그인이 필요하다.")
    @Test
    void test_index_page_need_to_authentication() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection());
    }

    @Order(8)
    @DisplayName("8. 비밀번호 오류 로그인")
    @ParameterizedTest
    @CsvSource(value = {"rolroralra:1234", "admin:1234"}, delimiterString = ":")
    void test_login_with_wrong_password(String username, String password) throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/login?error=true"));
    }

    @Order(9)
    @DisplayName("9. 로그인 비밀번호 오류로 인한 로그인 페이지 화면")
    @Test
    void test_login_page_with_wrong_password_error_message() throws Exception {
        mockMvc.perform(get("/login?error=true"))
                .andExpect(status().isOk());
    }

    private UserDetails generateUser(String username, String password, String... roles) {
        return User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(roles)
                .build();
    }

    private UserDetails testUser() {
        return generateUser("test", "1234", "TEST");
    }
}
