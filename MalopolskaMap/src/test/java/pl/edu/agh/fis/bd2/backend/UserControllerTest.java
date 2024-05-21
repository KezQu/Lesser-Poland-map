package pl.edu.agh.fis.bd2.backend;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Class providing tests to check correctness of exposed endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
	@Autowired
	private MockMvc mockMvc;
	/**
	 * Checks if correct response is returned based on the provided incorrect request
	 * @throws Exception
	 */
	@Test
	public void RegisterIncorrectBodyTest() throws Exception {
		mockMvc.perform(post("/register").with(csrf()).content("username=test&password="))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/register").with(csrf()).content("username=&password=test"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/register").with(csrf()).content(""))
				.andExpect(status().isBadRequest());
	}
	/**
	 * Checks if correct response is returned based on the provided correct request with mock user data to check registration mechanism
	 * @throws Exception
	 */
	@Test
	public void RegisterCorrectBodyTest() throws Exception {
		mockMvc.perform(post("/register").with(csrf()).content("username=test&password=test"))
				.andExpect(status().isFound())
				.andExpect(content().contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8)))
				.andExpect(content().string("user created successfully"));
	}

	/**
	 * Deletes mocked user registration
	 */
	@AfterAll
	public static void CleanMockUser(){
		JdbcClient conn = JdbcClient.create(DatabaseController.dataSource);
		conn.sql("DELETE FROM UserCred.UserRoles WHERE user_id = (SELECT id FROM UserCred.UserLoginData WHERE email LIKE 'test' AND password LIKE 'test');").update();
		conn.sql("DELETE FROM UserCred.UserLoginData WHERE email LIKE 'test' AND password LIKE 'test';").update();

	}
}