package pl.edu.agh.fis.bd2.frontend;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Base class providing mechanisms to effect thymeleaf templates
 */
@Controller
@RequestMapping
abstract public class TemplateController {
	/**
	 * Method allowing to change between views for logged and anonymous user
	 * @param model Spring model providing tools to affect template
	 * @return Value for a specified model attribute to be affected
	 */
	@ModelAttribute("menu_style")
	protected String MenuView(Model model){
		Collection<? extends GrantedAuthority> auth = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		if(!auth.toString().contains("USER"))
			return "visibility: hidden;";
		else{
			return "visibility: visible;";
		}
	}
	/**
	 * Method allowing to change between views for logged and anonymous user
	 * @param model Spring model providing tools to affect template
	 * @return Value for a specified model attribute to be affected
	 */
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
