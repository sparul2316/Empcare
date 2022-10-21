package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRep;
	
	//handler for home page
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Empcare");
		return "home";
	}
	
	
	//handler for about page
	@RequestMapping("/about")
	public String about(Model model) {
		
		model.addAttribute("title", "About");
		return "about";
	}
	
	
	//handler for signup page
	@RequestMapping("/signup")
	public String signup(Model model) {
		
		model.addAttribute("title", "Register");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	
	//handler for registering user
	
	@RequestMapping(value="/do_register" , method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult results,
			@RequestParam(value="agreement" , defaultValue="false") boolean agreement, 
			Model model,  HttpSession session) {
		
		try {
			
			
		
			
			if(!agreement) {
				System.out.println("You have not agreed to the terms and conditons!");
				throw new Exception("You have not agreed to the terms and conditons!");
			}
			
			
			if(results.hasErrors()) {
				
				System.out.println("error " + results.toString());
				
				model.addAttribute("user", user);
				
				return "signup";
				
			}
			
			System.out.println( "error " +  results.hasErrors());
			
			//left properties;
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("banner.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			
			User res =this.userRep.save(user);
			
			
			
			//so that the do_register page has autofilled values of user
			
			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfullly registered !! " ,"alert-success"));
			
			System.out.println(res);
			
			return "signup";
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong !! " + e.getMessage(), "alert-danger"));
			
			return "signup";
		}
		
		
	
	
	}
	
	
	
	
	//handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model) {
		
		model.addAttribute("title", "Login Page");
		return "login";
	}
	
	
}



