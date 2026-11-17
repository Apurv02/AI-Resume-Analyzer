package com.apurv.resumeanalyzer.service;

import com.apurv.resumeanalyzer.model.AppUser;
import com.apurv.resumeanalyzer.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    public RegistrationService(UserRepository users, PasswordEncoder encoder) {
        this.users = users; this.encoder = encoder;
    }

    public void register(String name, String email, String password) {
        String normalized = email.trim().toLowerCase();
        if (users.existsByEmail(normalized)) throw new IllegalArgumentException("An account with this email already exists.");
        AppUser user = new AppUser();
        user.setName(name.trim());
        user.setEmail(normalized);
        user.setPassword(encoder.encode(password));
        user.setRole("USER");
        users.save(user);
    }
}
