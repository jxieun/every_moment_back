package com.rookies4.every_moment.entity;

import com.rookies4.every_moment.match.entity.Preference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
                @Index(name = "idx_users_email", columnList = "email")
}, uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, length = 40)
        private String username;

        @Column(nullable = false)
        private Integer gender; // 0 = 남성, 1 = 여성 <-- [추가된 부분]

        @Column(nullable = false, length = 100)
        private String email;

        @Column(name = "password_hash", nullable = false, length = 255)
        private String passwordHash;

        @Column(nullable = false)
        private Boolean smoking; // 0=false, 1=true

        @Column(nullable = false, length = 20)
        private String role;

        @Column(nullable = false)
        private Boolean active;

        @Column(name = "room_number", length = 10)
        private String roomNumber; // 자동 배정되는 호실 (예: "101", "102")

        // 사용자 Preference 관계 (1:1 관계, nullable 설정)
        @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @JoinColumn(name = "preference_id", nullable = true) // 외래 키를 설정하고, null 허용
        private Preference preference;

        @CreatedDate
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @LastModifiedDate
        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt;

        @PrePersist
        public void prePersist() {
                LocalDateTime now = LocalDateTime.now();
                if (createdAt == null)
                        createdAt = now;
                if (updatedAt == null)
                        updatedAt = now;

                if (active == null)
                        active = true;
                if (role == null)
                        role = "ROLE_USER";
                if (smoking == null)
                        smoking = false;
        }

        @PreUpdate
        public void preUpdate() {
                updatedAt = LocalDateTime.now();
        }
}