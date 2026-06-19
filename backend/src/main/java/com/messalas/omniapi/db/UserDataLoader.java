package com.messalas.omniapi.db;

import com.messalas.omniapi.model.entities.UserEntity;
import com.messalas.omniapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration(proxyBeanMethods = false)
@Order(1)
public class UserDataLoader {

    private static final Logger log = LoggerFactory.getLogger(UserDataLoader.class);

    @Value("${omniapi.admin.username:admin}")
    private String adminUsername;

    @Value("${omniapi.admin.password:admin}")
    private String adminPassword;

    @Bean
    CommandLineRunner seedDefaultUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                UserEntity admin = UserEntity.builder()
                        .username(adminUsername)
                        .password(passwordEncoder.encode(adminPassword))
                        .role("ROLE_ADMIN")
                        .build();
                userRepository.save(admin);
                log.info("Seeded default admin user: {}", adminUsername);
            }
        };
    }
}
