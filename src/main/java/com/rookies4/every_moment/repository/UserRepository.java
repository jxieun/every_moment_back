package com.rookies4.every_moment.repository;

import com.rookies4.every_moment.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);

    // 성별(gender)과 흡연 여부(smoking)로 필터링된 사용자 리스트를 반환하는 메소드
    List<UserEntity> findByGenderAndSmoking(Integer gender, Boolean smoking);

    // 호실 관리용 쿼리
    @org.springframework.data.jpa.repository.Query("SELECT MAX(CAST(u.roomNumber AS int)) FROM UserEntity u WHERE u.roomNumber IS NOT NULL")
    Integer findMaxRoomNumber();

    boolean existsByRoomNumber(String roomNumber);

    // 특정 층(범위) 내에서 가장 큰 호실 번호 조회
    @org.springframework.data.jpa.repository.Query("SELECT MAX(CAST(u.roomNumber AS int)) FROM UserEntity u WHERE u.roomNumber IS NOT NULL AND CAST(u.roomNumber AS int) BETWEEN :start AND :end")
    Integer findMaxRoomNumberInRange(@org.springframework.data.repository.query.Param("start") int start,
            @org.springframework.data.repository.query.Param("end") int end);
}