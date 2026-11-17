package com.apurv.resumeanalyzer.controller;

import com.apurv.resumeanalyzer.model.AppUser;
import com.apurv.resumeanalyzer.model.ResumeAnalysis;
import com.apurv.resumeanalyzer.repository.UserRepository;
import com.apurv.resumeanalyzer.service.ResumeAnalysisService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.util.List;

@Controller
public class DashboardController {
    private final UserRepository users;
    private final ResumeAnalysisService service;
    public DashboardController(UserRepository users, ResumeAnalysisService service) {
        this.users = users; this.service = service;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        AppUser user = currentUser(auth);
        List<ResumeAnalysis> records = service.history(user.getId());
        int average = records.isEmpty() ? 0 : (int)Math.round(records.stream().mapToInt(ResumeAnalysis::getScore).average().orElse(0));
        model.addAttribute("user", user);
        model.addAttribute("records", records.stream().limit(5).toList());
        model.addAttribute("total", records.size());
        model.addAttribute("average", average);
        model.addAttribute("lastScore", records.isEmpty() ? null : records.get(0).getScore());
        return "dashboard";
    }

    @PostMapping("/analyses")
    public String create(@RequestParam String resumeName, @RequestParam String jobDescription,
                         @RequestParam("resume") MultipartFile file, Authentication auth,
                         RedirectAttributes redirect) {
        try {
            AppUser user = currentUser(auth);
            if (resumeName == null || resumeName.isBlank()) throw new IllegalArgumentException("Enter a name for this analysis.");
            if (jobDescription == null || jobDescription.trim().length() < 30) throw new IllegalArgumentException("Please enter a more detailed job description (at least 30 characters).");
            ResumeAnalysis record = service.create(user.getId(), user.getEmail(), resumeName, jobDescription, file);
            redirect.addFlashAttribute("success", "Analysis completed. Your match score is " + record.getScore() + "%.");
        } catch (IllegalArgumentException | IOException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "Analysis could not be completed. Check MongoDB and AI configuration, then try again.");
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/history")
    public String history(Authentication auth, Model model) {
        AppUser user = currentUser(auth);
        model.addAttribute("user", user);
        model.addAttribute("records", service.history(user.getId()));
        return "history";
    }

    @PostMapping("/analyses/{id}/delete")
    public String delete(@PathVariable String id, Authentication auth, RedirectAttributes redirect) {
        try {
            service.delete(id, currentUser(auth).getId());
            redirect.addFlashAttribute("success", "Analysis deleted.");
        } catch (IllegalArgumentException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/history";
    }

    private AppUser currentUser(Authentication auth) {
        return users.findByEmail(auth.getName().toLowerCase()).orElseThrow();
    }
}
