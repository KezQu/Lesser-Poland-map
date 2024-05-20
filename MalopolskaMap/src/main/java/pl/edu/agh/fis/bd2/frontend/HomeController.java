package pl.edu.agh.fis.bd2.frontend;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.fis.bd2.spatialTypes.Area;

import java.util.List;

@Controller
@RequestMapping
public class HomeController extends TemplateController {

	@GetMapping("/")
	public String HomeGet(Model model){
		if(SecurityContextHolder.getContext().getAuthentication().isAuthenticated())
			model.addAttribute("verified", true);
		return "main";
	}

	@GetMapping("/guest")
	public String GuestGet(Model model){
		return "main";
	}
}
