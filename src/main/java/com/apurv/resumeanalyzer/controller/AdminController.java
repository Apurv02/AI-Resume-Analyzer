package com.apurv.resumeanalyzer.controller;

import com.apurv.resumeanalyzer.repository.ResumeAnalysisRepository;
import com.apurv.resumeanalyzer.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {
    private final UserRepository users;
    private final ResumeAnalysisRepository analyses;
    public AdminController(UserRepository users, ResumeAnalysisRepository analyses) {
        this.users = users; this.analyses = analyses;
    }
    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("users", users.findAll());
        model.addAttribute("analyses", analyses.findAllByOrderByCreatedAtDesc());
        model.addAttribute("userCount", users.count());
        model.addAttribute("analysisCount", analyses.count());
        return "admin";
    }
}
