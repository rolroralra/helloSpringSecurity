package com.example.security;

import org.junit.jupiter.api.Test;
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
class DemoApplicationTests {
	private final MockMvc mockMvc;

	@Autowired
	public DemoApplicationTests(MockMvc mockMvc) {
		this.mockMvc = mockMvc;
	}

	@ParameterizedTest
	@CsvSource(value = {"image/jpeg:images:jpg", "application/javascript:js:js", "text/css:css:css"}, delimiterString = ":")
	void test_static_resource_without_login(String mediaType, String staticResourceLocation, String filenameExtension) throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/{staticResourceLocation}/test.{filenameExtension}", staticResourceLocation, filenameExtension))
				.andExpect(status().isOk())
				.andExpect(content().contentType(mediaType));
	}

	@ParameterizedTest
	@ValueSource(strings = {"/login"})
	void test_200_OK(String requestPath) throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get(requestPath))
				.andExpect(status().isOk());
	}

	@ParameterizedTest
	@ValueSource(strings = {"/hello", "/logout", "/temp", "/any"})
	void test_300_Redirection(String requestPath) throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get(requestPath))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	void test_login() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders
						.post("/login")
						.param("username", "admin")
						.param("password", "admin"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andDo(print());
	}
}
