package pl.edu.agh.fis.bd2.frontend;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Class providing basic layout of login and register page
 */
@Controller
@RequestMapping
public class AuthController extends TemplateController {
	/**
	 * Endpoint providing layout of a login page
	 * @param model Spring boot model providing tools to modify thymeleaf template
	 * @return Processed thymeleaf template to be generated for client
	 */
	@GetMapping("/login")
	public String LoginGet(Model model){
		model.addAttribute("auth_form_submit_value", "log in");
		model.addAttribute("auth_form_action", "/login");
		return "main";
	}
	/**
	 * Endpoint providing layout of a register page
	 * @param model Spring boot model providing tools to modify thymeleaf template
	 * @return Processed thymeleaf template to be generated for client
	 */
	@GetMapping("/register")
	public String RegisterGet(Model model){
		model.addAttribute("auth_form_submit_value", "Sign in");
		model.addAttribute("account_create_style", "visibility: hidden;");
		model.addAttribute("auth_form_action", "/register");

		return "main";
	}

}
