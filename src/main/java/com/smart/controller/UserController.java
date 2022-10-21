package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRep;
	
	@Autowired
	private ContactRepository contactRepo;
	
	// method to add common data to repository
	
	@ModelAttribute
	public void addCommonData(Model model , Principal principal) {
		
			String userName= principal.getName();
			
			//get the user using usernmae
			User user=userRep.getUserByUserName(userName);
			
			model.addAttribute("user", user);
	}
	
	//dashboard page
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		
		model.addAttribute("title", "Home");
		return "normal/user_dashboard";
	}
	
	
	//open add form handler
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title", "Add contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	//proceesing add contact form
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, 
			@RequestParam("profileImage") MultipartFile file,
			Principal principal , HttpSession session) {
		
		
		try {
			
			
			
			
			String name = principal.getName();
			
			User user = this.userRep.getUserByUserName(name);
			
			
			//processing and uploading file....
			if(file.isEmpty()) {
				System.out.println("File is empty");
				contact.setImage("contact.png");
				
			}
			
			else {
				
				contact.setImage(file.getOriginalFilename());
				
				
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				
				Files.copy(file.getInputStream() , path , StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
				
			}
			
			
			//one to many mapping
			user.getContacts().add(contact);
			contact.setUser(user);
			this.userRep.save(user);
			
			
			System.out.println("Added to database");
			System.out.println("DATA " + contact);
			
			//message success ......
			session.setAttribute("message", new Message("Contact added succesfully!", "success"));
				
			
			
			return "normal/add_contact_form";
			
		} catch (Exception e) {
			// TODO: handle exception
			
//			System.out.println("ERROR " + e.getMessage());
			
			e.printStackTrace();
			
			//message
			
			session.setAttribute("message", new Message("Something went wrong !! Try again...", "danger"));
			
			
		}
		
		
		
		
		return "normal/add_contact_form";
		
	}
	
	
	//show contact handler
	//per page we want 5 contacts(n)
	//current page has to be 0 (page)
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model model, Principal principal) {
		//we have to send the contact list
		
		model.addAttribute("title", "Your contacts");
		
		String userName = principal.getName();
		User user = this.userRep.getUserByUserName(userName);
		
		Pageable pageable = PageRequest.of(page, 5);
		
		Page<Contact>  contacts = this.contactRepo.findContactsByUser(user.getId(), pageable);
		
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		
		model.addAttribute("totalPages", contacts.getTotalPages());
//		System.out.println("total pages are " + contacts.getTotalPages());
		
		
		return "normal/show_contacts";
	}
	
	
	//showing a specific contact
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		
		
		
		Optional<Contact> contactOp = this.contactRepo.findById(cId);
		
		Contact contact = contactOp.get();
		
		//getting the user -- to make sure this contact belongs to the right user;
		
		String userName = principal.getName();
		
		User user =this.userRep.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact", contact);
		}
		
		return "normal/contact_detail";
	}
	
	
	//delete contact handler
	
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal, HttpSession session) {
		
		Optional<Contact> contactOp = this.contactRepo.findById(cId);
		
		Contact contact = contactOp.get();
		

		String userName = principal.getName();
		
		User user =this.userRep.getUserByUserName(userName);
		
		
		//check.. that it is the rigth user
		if(user.getId()==contact.getUser().getId()) {
			
			contact.setUser(null);
			this.contactRepo.delete(contact);
			session.setAttribute("message", new Message("Contact deleted successfully!" , "success"));
			
		}
		return "redirect:/user/show-contacts/0";
	
	}
	
	
	//opening update form handler
	
	@PostMapping("/update-contact/{cId}")
	public String updateForm( @PathVariable("cId") Integer cId, Model model) {
		
		model.addAttribute("title", "Update Contact");
		
		Contact contact = this.contactRepo.findById(cId).get();
		
		model.addAttribute(contact);
		return "normal/update_form";
	}
	
	
	//processing update form handler
	
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact , 
			@RequestParam("profileImage") MultipartFile file, 
			Model model,
			HttpSession session,
			Principal principal) {
		
		
		
		try {
			//old contact detail
			
			Contact oldContact = this.contactRepo.findById(contact.getcId()).get();
			
			
			if(!file.isEmpty()) {
				//rewrite the file
				
//				delete old photo
				
				File deleteFile = new ClassPathResource("static/img").getFile();
				
				File file1 = new File(deleteFile , oldContact.getImage());
				file1.delete();
				
				
				
				//update new photo

				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				
				Files.copy(file.getInputStream() , path , StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
				
			}else {
				
				contact.setImage(oldContact.getImage());
			}
			
			User user = this.userRep.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			this.contactRepo.save(contact);
			session.setAttribute("message", new Message("Contact updated!", "success"));
			
			
			return "redirect:/user/" + contact.getcId() + "/contact";
			
		} catch (Exception e) {
			// TODO: handle exception
			
			e.printStackTrace();
		}
		
		
		//cid wont be recieved from the update for as we didn't 
		//provide it as an attribute -> use hidden form fields
		return "redirect:/user/" + contact.getcId() + "/contact";
	}
	
	
	
	
	
}
