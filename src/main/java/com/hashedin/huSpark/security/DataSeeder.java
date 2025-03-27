package com.hashedin.huSpark.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hashedin.huSpark.entity.Role;
import com.hashedin.huSpark.entity.User;
import com.hashedin.huSpark.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.Calendar;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedSuperAdminUser();
    }

    private void seedSuperAdminUser() {
        String adminEmail = "superadmin@socio.com";

        // Check if the admin user already exists
        Optional<User> existingAdmin = userRepository.findByEmail(adminEmail);
        if (existingAdmin.isPresent()) {
            System.out.println("Super Admin user already exists. Skipping seeding.");
            return;
        }

        // Create the admin user
        User adminUser = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode("Admin@123")) // Strong default password
                .isAdmin(true)
                .role(Role.ADMIN)
                .isProfilePrivate(true)
                .dateOfBirth(new Calendar.Builder().setDate(1872, Calendar.SEPTEMBER, 5).build().getTime())
                .build();

        userRepository.save(adminUser);

        System.out.println("Super Admin user seeded successfully!");
    }
}
