package pl.edu.agh.fis.bd2.frontend;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping
public class AuthController extends TemplateController {
	@GetMapping("/login")
	public String LoginGet(Model model){
		model.addAttribute("auth_form_submit_value", "log in");
		model.addAttribute("auth_form_action", "/login");
		return "main";
	}
	@GetMapping("/register")
	public String RegisterGet(Model model){
		model.addAttribute("auth_form_submit_value", "Sign in");
		model.addAttribute("account_create_style", "visibility: hidden;");
		model.addAttribute("auth_form_action", "/register");

		return "main";
	}

}
