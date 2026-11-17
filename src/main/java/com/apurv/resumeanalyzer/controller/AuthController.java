package com.apurv.resumeanalyzer.controller;

import com.apurv.resumeanalyzer.model.AppUser;
import com.apurv.resumeanalyzer.repository.UserRepository;
import com.apurv.resumeanalyzer.service.RegistrationService;
import jakarta.validation.constraints.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@Validated
public class AuthController {
    private final RegistrationService registration;
    private final UserRepository users;
    public AuthController(RegistrationService registration, UserRepository users) {
        this.registration = registration; this.users = users;
    }

    @GetMapping("/")
    public String home() { return "redirect:/dashboard"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("form") RegisterForm form, BindingResult errors, Model model) {
        if (form.name == null || form.name.trim().length() < 2) errors.rejectValue("name","name.invalid","Enter your name.");
        if (form.email == null || !form.email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) errors.rejectValue("email","email.invalid","Enter a valid email.");
        if (form.password == null || form.password.length() < 8) errors.rejectValue("password","password.invalid","Password must be at least 8 characters.");
        if (!java.util.Objects.equals(form.password, form.confirmPassword)) errors.rejectValue("confirmPassword","password.mismatch","Passwords do not match.");
        if (errors.hasErrors()) return "register";
        try {
            registration.register(form.name, form.email, form.password);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("registrationError", ex.getMessage());
            return "register";
        }
    }

    public static class RegisterForm {
        public String name = "";
        public String email = "";
        public String password = "";
        public String confirmPassword = "";
        public String getName(){return name;} public void setName(String v){name=v;}
        public String getEmail(){return email;} public void setEmail(String v){email=v;}
        public String getPassword(){return password;} public void setPassword(String v){password=v;}
        public String getConfirmPassword(){return confirmPassword;} public void setConfirmPassword(String v){confirmPassword=v;}
    }
}
