package com.apurv.resumeanalyzer.config;

import com.apurv.resumeanalyzer.model.AppUser;
import com.apurv.resumeanalyzer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner createAdmin(UserRepository users, PasswordEncoder encoder,
                                  @Value("${app.admin.email:}") String adminEmail,
                                  @Value("${app.admin.password:}") String adminPassword) {
        return args -> {
            if (adminEmail != null && !adminEmail.isBlank()
                    && adminPassword != null && !adminPassword.isBlank()
                    && users.findByEmail(adminEmail.toLowerCase()).isEmpty()) {
                AppUser admin = new AppUser();
                admin.setName("Administrator");
                admin.setEmail(adminEmail.toLowerCase());
                admin.setPassword(encoder.encode(adminPassword));
                admin.setRole("ADMIN");
                users.save(admin);
            }
        };
    }
}
