package com.example.jobboard.controller;

import com.example.jobboard.dto.UserForm;
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
    public String register(@RequestParam("role") String role, Model model) {
        UserForm userForm = new UserForm();
        if (role.equalsIgnoreCase("candidate")) {
            userForm.setRole("CANDIDATE");
        } else if (role.equalsIgnoreCase("employer")) {
            userForm.setRole("EMPLOYER");
        }
        model.addAttribute("userForm", userForm);
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("userForm") UserForm userForm,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        registrationService.register(userForm);
        return "redirect:/login";
    }
}
