package pl.edu.agh.fis.bd2.frontend;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping
abstract public class TemplateController {
	@ModelAttribute("menu_style")
	protected String MenuView(Model model){
		Collection<? extends GrantedAuthority> auth = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		if(!auth.toString().contains("USER"))
			return "visibility: hidden;";
		else{
			return "visibility: visible;";
		}
	}
	@ModelAttribute("user_logged_in")
	protected String LoggedView(Model model){
		Collection<? extends GrantedAuthority> auth = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		if(!auth.toString().contains("USER"))
			return "";
		else{
			return "visibility: hidden;";
		}
	}

}
