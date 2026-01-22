package com.rookies4.every_moment;

import com.rookies4.every_moment.entity.UserEntity;
import com.rookies4.every_moment.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class EveryMomentApplication {

    public static void main(String[] args) {
        SpringApplication.run(EveryMomentApplication.class, args);
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Seoul"));
    }

    @Bean
    CommandLineRunner seed(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (users.findByEmail("admin@example.com").isEmpty()) {
                users.save(UserEntity.builder()
                        .username("admin")
                        .gender(0)
                        .email("admin@example.com")
                        .passwordHash(encoder.encode("AdminPassw0rd!"))
                        .role("ROLE_ADMIN")
                        .smoking(false)
                        .active(true)
                        .build());
            }
            if (users.findByEmail("demo@example.com").isEmpty()) {
                users.save(UserEntity.builder()
                        .username("demo")
                        .gender(1) // 성추가
                        .email("demo@example.com")
                        .passwordHash(encoder.encode("Passw0rd!"))
                        .role("ROLE_USER")
                        .smoking(false)
                        .active(true)
                        .build());
            }
        };
    }
}