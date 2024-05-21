package pl.edu.agh.fis.bd2.frontend;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Class providing layout of a home page for authenticated user
 */
@Controller
@RequestMapping
public class HomeController extends TemplateController {
	/**
	 * Endpoint generating homepage for logged user
	 * @param model Spring boot model providing access to specified thymeleaf template attributes
	 * @return Thymeleaf template generating homepage
	 */
	@GetMapping("/")
	public String HomeGet(Model model){
		if(SecurityContextHolder.getContext().getAuthentication().isAuthenticated())
			model.addAttribute("verified", true);
		return "main";
	}
}
