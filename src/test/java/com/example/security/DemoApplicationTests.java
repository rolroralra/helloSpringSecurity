package com.example.security;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DemoApplicationTests {
	private final MockMvc mockMvc;

	@Autowired
	public DemoApplicationTests(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@Order(1)
	@DisplayName("1. Static Resource를 로그인 없이 접근할 수 있다.")
	@ParameterizedTest
	@CsvSource(value = {"image/jpeg:images:jpg", "application/javascript:js:js", "text/css:css:css"}, delimiterString = ":")
	void test_static_resource_without_login(String mediaType, String staticResourceLocation, String filenameExtension) throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/{staticResourceLocation}/test.{filenameExtension}", staticResourceLocation, filenameExtension))
				.andExpect(status().isOk())
				.andExpect(content().contentType(mediaType));
	}

	@Order(2)
	@DisplayName("2. 로그인 페이지는 로그인 없이 접근할 수 있다.")
	@ParameterizedTest
	@ValueSource(strings = {"/login"})
	void test_200_OK(String requestPath) throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get(requestPath))
				.andExpect(status().isOk());
	}


	@Order(3)
	@DisplayName("3. 다른 페이지는 로그인 없이 접근할 수 없다. (3xx Redirection)")
	@ParameterizedTest
	@ValueSource(strings = {"/hello", "/logout", "/temp", "/any"})
	void test_300_Redirection(String requestPath) throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get(requestPath))
				.andExpect(status().is3xxRedirection());
	}

	@Order(4)
	@DisplayName("4. 올바른 username, password를 통해 정상적으로 로그인 할 수 있다.")
	@ParameterizedTest
	@CsvSource(value = {"rolroralra:chrlghk123#", "admin:admin"}, delimiterString = ":")
	void test_login(String username, String password) throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders
						.post("/login")
						.param("username", username)
						.param("password", password))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andDo(print());
	}
}
