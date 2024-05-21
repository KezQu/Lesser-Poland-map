package pl.edu.agh.fis.bd2.backend;

import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.fis.bd2.SecurityConfig;
import pl.edu.agh.fis.bd2.UserData;

/**
 * Class providing endpoints for user registering
 */
@RestController
@RequestMapping
public class UserController extends DatabaseController{
	/**
	 * Endpoint that allows to verify provided credentials and to create new user
	 * @param body Body request containing provided credentials
	 * @param model Spring boot model that allows to change thymeleaf template
	 * @return Server response signaling whether user was created successfully or some error was made along the way
	 */
	@PostMapping("/register")
	public ResponseEntity<String> RegisterPost(@RequestBody String body, Model model){
		UserData createdUser = new UserData();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Location", "/login");
		try{
			JSONObject bodyJSON = new JSONObject();
			for(String object : body.split("&")){
				String[] KeyVal = object.split("=");
				if(KeyVal.length != 2){
					throw new RequestRejectedException("email and password cannot be empty");
				}
				bodyJSON.put(KeyVal[0], KeyVal[1]);
			}
			createdUser.setEmail(bodyJSON.get("username").toString().replace("%40", "@"));
			createdUser.setPassword(bodyJSON.get("password").toString());
			createdUser.setRoles(new String[]{"USER"});
			conn.sql("""
					EXEC dbo.Register '""" +
					createdUser.getEmail() + "', '" +
					createdUser.getPassword() + "', '" +
					createdUser.getRoles() + "';").update();
		}catch (RuntimeException e){
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_PLAIN).body(e.getMessage());
		}
		UserDetailsManager userDetailsManager = SecurityConfig.inMemoryUserDetailsManager;
		userDetailsManager.createUser(User.withDefaultPasswordEncoder()
				.username(createdUser.getEmail())
				.password(createdUser.getPassword())
				.roles(createdUser.getRoles().split(","))
				.build());

		return ResponseEntity.status(HttpStatus.FOUND).headers(headers).body("user created successfully"); // FOUND
	}
}
