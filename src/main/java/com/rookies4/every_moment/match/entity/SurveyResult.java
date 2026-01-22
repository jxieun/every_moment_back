package com.rookies4.every_moment.match.entity;

import com.rookies4.every_moment.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "survey_results")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SurveyResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // 설문을 제출한 사용자

    @Column(nullable = false)
    private Integer sleepTime; // 1. 10시이후, 2. 1시 이후, 3. 3시 이후

    @Column(nullable = false)
    private Integer cleanliness; // 1. 5-6회, 2. 3-4회, 3. 1-2회, 4. 하지 않음

    @Column(nullable = false)
    private Integer noiseSensitivity; // 1. 예민함, 2. 보통, 3. 민감하지 않음

    @Column(nullable = false)
    private Integer height; // 1. 저층, 2. 중간, 3. 고층

    @Column(nullable = false)
    private Integer roomTemp; // 1. 20도 미만/22도 미만, 2. 20도-24도/22도-26도, 3. 24도 초과/26도 초과

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
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}