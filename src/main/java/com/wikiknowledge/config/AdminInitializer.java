package com.wikiknowledge.config;

import com.wikiknowledge.domain.User;
import com.wikiknowledge.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;/** 管理员种子账号初始化器 */


@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AdminInitializer(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${app.admin.username:admin}") String adminUsername,
                            @Value("${app.admin.password:admin123}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    /**
     * 应用启动时创建管理员账号，账号已存在则跳过。
     */
    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }
        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setDisplayName("系统管理员");
        admin.setRole("ROLE_ADMIN");
        userRepository.save(admin);
        log.info("Seeded admin user: {}", adminUsername);
    }
}
