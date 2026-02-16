package com.rookies4.every_moment.match.repository;

import com.rookies4.every_moment.match.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    // 상태별 매칭 결과 조회 (예: PENDING 상태)
    List<MatchResult> findByStatus(String status);

    // 매칭 결과를 점수 기준으로 조회
    List<MatchResult> findByUserIdOrderByScoreDesc(Long userId);

    // 사용자와 관련된 모든 매칭 결과를 가져오는 메서드
    List<MatchResult> findByUserId(Long userId);

    // 사용자와 상대방 매칭 결과를 가져오는 메서드 (여러 결과 반환)
    @Query("SELECT m FROM MatchResult m WHERE m.user.id = :userId AND m.matchUser.id = :matchUserId")
    List<MatchResult> findByUserIdAndMatchUserId(@Param("userId") Long userId, @Param("matchUserId") Long matchUserId);
}

