package com.example.jobboard.controller;

import com.example.jobboard.dto.UserForm;
import com.example.jobboard.exception.EmailAlreadyExistException;
import com.example.jobboard.service.RegistrationService;
import com.example.jobboard.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegisterController {

    private final UserService userService;
    private final RegistrationService registrationService;

    @Autowired
    public RegisterController(UserService userService, RegistrationService registrationService) {
        this.userService = userService;
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String register(@RequestParam(value = "role",required = false) String role, Model model) {
        UserForm userForm = new UserForm();
        if ("candidate".equalsIgnoreCase(role)) {
            userForm.setRole("CANDIDATE");
        } else if ("employer".equalsIgnoreCase(role)) {
            userForm.setRole("EMPLOYER");
        }
        model.addAttribute("userForm", userForm);
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("userForm") UserForm userForm,
                           BindingResult bindingResult) {
        if("employer".equalsIgnoreCase(userForm.getRole())) {
            if(userForm.getCompanyName() == null || userForm.getCompanyName().isEmpty()) {
                bindingResult.rejectValue("companyName","error.userForm", "Company name is required");
            }
            if(userForm.getCountry() == null || userForm.getCountry().isEmpty()) {
                bindingResult.rejectValue("country","error.userForm", "country name is required");
            }
            if(userForm.getCity() == null || userForm.getCity().isEmpty()) {
                bindingResult.rejectValue("city","error.userForm", "city name is required");
            }
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }
        try{
            registrationService.register(userForm);
        }catch (EmailAlreadyExistException ex){
            bindingResult.rejectValue("email","error.userForm", "Email already exists");
            return "register";
        }
        return "redirect:/login";
    }
}
