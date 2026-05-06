package com.careerconnect.config;

import com.careerconnect.entity.Job;
import com.careerconnect.entity.User;
import com.careerconnect.enums.JobStatus;
import com.careerconnect.enums.Role;
import com.careerconnect.repository.JobRepository;
import com.careerconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // ─── Admin ────────────────────────────────────────────────────
        if (userRepository.findByEmail("admin@cc.com").isEmpty()) {
            User admin = new User();
            admin.setName("Platform Admin");
            admin.setEmail("admin@cc.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);
            System.out.println("✅ Default admin created: admin@cc.com / admin123");
        }

        // ─── Employer ─────────────────────────────────────────────────
        if (userRepository.findByEmail("employer@cc.com").isEmpty()) {
            User employer = new User();
            employer.setName("Global Tech Solutions");
            employer.setEmail("employer@cc.com");
            employer.setPassword(passwordEncoder.encode("employer123"));
            employer.setRole(Role.EMPLOYER);
            employer.setCompany("Global Tech Solutions");
            employer.setCreatedAt(LocalDateTime.now());
            userRepository.save(employer);
            System.out.println("✅ Default employer created: employer@cc.com / employer123");
        }
    }
}
