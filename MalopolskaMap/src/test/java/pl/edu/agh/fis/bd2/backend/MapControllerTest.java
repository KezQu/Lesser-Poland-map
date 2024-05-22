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
 * Class providing tests to check correctness of exposed endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
class MapControllerTest {
	@Autowired
	private MockMvc mockMvc;

	/**
	 * Checks if basic voivodeship borders are returned as correct response
	 * @throws Exception
	 */
	@Test
	public void VoivodeshipAreaTest() throws Exception {
		mockMvc.perform(get("/voivodeshipborder/"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

	/**
	 * Checks if bounding box of voivodeship are returned as correct response
	 * @throws Exception
	 */
	@Test
	public void BoundingBoxTest() throws Exception {
		mockMvc.perform(get("/voivodeshipborder/boundingbox"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}
	/**
	 * Checks if correct response is returned based on the provided incorrect request
	 * @throws Exception
	 */
	@Test
	public void IsInsideIncorrectBodyTest() throws Exception {
		mockMvc.perform(post("/operation/isinside").with(csrf()).content(""))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/operation/isinside").with(csrf()).content("{\"x\":\"asd\",\"y\":10.0}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/operation/isinside").with(csrf()).content("{\"x\":\"10.0\",\"y\":asd}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/operation/isinside").with(csrf()).content("{\"y\":10.0}"))
				.andExpect(status().isBadRequest());
	}
	/**
	 * Checks if correct response is returned based on the provided correct request
	 * @throws Exception
	 */
	@Test
	public void IsInsideCorrectBodyTest() throws Exception {
		mockMvc.perform(post("/operation/isinside").with(csrf()).content("{\"x\":10.0,\"y\":10.0}"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
		mockMvc.perform(post("/operation/isinside").with(csrf()).content("{\"x\":\"10.0\",\"y\":10.0, \"z\":asdascxzxc, \"asdcxz\":[]}"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

}