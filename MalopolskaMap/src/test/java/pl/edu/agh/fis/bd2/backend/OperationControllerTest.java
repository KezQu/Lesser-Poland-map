package pl.edu.agh.fis.bd2.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Class providing tests for operations on spatial types
 */
@SpringBootTest
@AutoConfigureMockMvc
class OperationControllerTest {
	@Autowired
	private MockMvc mockMvc;
	/**
	 * Checks if correct response is returned based on the provided incorrect request
	 * @throws Exception
	 */
	@Test
	public void AreaPostIncorrectBodyTest() throws Exception {
		mockMvc.perform(post("/operation/area").with(csrf()).content(""))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/operation/area").with(csrf()).content("[{\"x\":0.0, \"y\":0.0},{\"x\":1.0, \"y\":0.0},{\"x\":1.0, \"y\":1.0}]"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/operation/area").with(csrf()).content("[{\"x\":0.0, \"y\":0.0},{\"x\":1.0, \"y\":0.0}]"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/operation/area").with(csrf()).content("[{\"x\":asd, \"y\":0.0},{\"x\":1.0, \"y\":0.0},{\"x\":1.0, \"y\":1.0},{\"x\":0.0, \"y\":0.0}]"))
				.andExpect(status().isBadRequest());
	}
	/**
	 * Checks if correct response is returned based on the provided correct request and calculated area value is valid
	 * @throws Exception
	 */
	@Test
	public void AreaPostCorrectBodyTest() throws Exception {
		mockMvc.perform(post("/operation/area").with(csrf()).content("[{\"x\":0.0, \"y\":0.0},{\"x\":1.0, \"y\":0.0},{\"x\":1.0, \"y\":1.0},{\"x\":0.0, \"y\":0.0}]"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(content().json("{\"area\":0.5}"));
	}
	/**
	 * Checks if correct response is returned based on the provided incorrect request
	 * @throws Exception
	 */
	@Test
	public void DistancePostIncorrectBodyTest() throws Exception {
		mockMvc.perform(post("/operation/distance").with(csrf()).content(""))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/operation/distance").with(csrf()).content("{\"p1\":{\"x\":0.0, \"y\":0.0}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/operation/distance").with(csrf()).content("{\"p1\":{\"x\":asd, \"y\":0.0},\"p2\":{\"x\":1.0, \"y\":0.0}}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/operation/distance").with(csrf()).content("{\"p1\":{\"x\":0.0, \"y\":0.0},\"asdczxc\":{\"x\":1.0, \"y\":0.0}}"))
				.andExpect(status().isBadRequest());
	}
	/**
	 * Checks if correct response is returned based on the provided correct request and calculated distance between points is valid
	 * @throws Exception
	 */
	@Test
	public void DistancePostCorrectBodyTest() throws Exception {
		mockMvc.perform(post("/operation/distance").with(csrf()).content("{\"p1\":{\"x\":0.0, \"y\":0.0},\"p2\":{\"x\":1.0, \"y\":0.0}}"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(content().json("{\"distance\":1.0}"));
	}
}