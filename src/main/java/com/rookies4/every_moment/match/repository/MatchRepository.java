package com.rookies4.every_moment.match.repository;

import com.rookies4.every_moment.entity.UserEntity;
import com.rookies4.every_moment.match.entity.Match;
import com.rookies4.every_moment.match.entity.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    // 특정 사용자와 매칭된 모든 매칭 조회
    List<Match> findByUser1_Id(Long userId);

    List<Match> findByUser2_Id(Long userId);

    // 매칭 상태로 조회 (예: PENDING, ACCEPTED)
    List<Match> findByStatus(String status);

    // 사용자 1, 사용자 2, 매칭 상태로 매칭을 찾는 메서드
    Optional<Match> findByUser1AndUser2AndStatus(UserEntity user1, UserEntity user2, MatchStatus status);

    // 두 사용자 간의 매칭 조회
    List<Match> findByUser1_IdAndUser2_Id(Long user1Id, Long user2Id);

    // 사용자 1 또는 사용자 2와 매칭된 결과 조회 (나의 매칭 상태 확인)
    @Query("SELECT m FROM Match m WHERE m.user1.id = :userId OR m.user2.id = :userId")
    List<Match> findByUser1_IdOrUser2_Id(@Param("userId") Long userId);

    // 사용자 1과 사용자 2의 ID로 매칭을 조회하는 메서드
    @Query("SELECT m FROM Match m WHERE (m.user1.id = :userId AND m.user2.id = :matchUserId) OR (m.user1.id = :matchUserId AND m.user2.id = :userId)")
    List<Match> findByUser1IdAndUser2Id(@Param("userId") Long userId, @Param("matchUserId") Long matchUserId);

    // proposeMatch 양방향 제안 확인
    @Query("SELECT COUNT(m) > 0 FROM Match m " +
            "WHERE ((m.user1.id = :userA AND m.user2.id = :userB) " +
            "   OR (m.user1.id = :userB AND m.user2.id = :userA)) " +
            "AND m.status = :status")
    boolean existsPendingMatchBetweenUsers(@Param("userA") Long userAId, @Param("userB") Long userBId,
            @Param("status") MatchStatus status);

    // 사용자 1이 매칭된 상태인지 확인하는 메서드 (ACCEPTED 상태)
    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE (m.user1.id = :userId OR m.user2.id = :userId) AND m.status = :status")
    boolean existsByUserAndStatus(@Param("userId") Long userId, @Param("status") MatchStatus status);

    // 사용자 1이 매칭된 상태인지 확인하는 메서드 (ACCEPTED 상태)
    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE m.user1.id = :targetUserId AND m.status = :matchStatus")
    boolean existsByUser1AndStatus(@Param("targetUserId") Long targetUserId,
            @Param("matchStatus") MatchStatus matchStatus);

    // 사용자 2가 매칭된 상태인지 확인하는 메서드 (ACCEPTED 상태)
    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE m.user2.id = :targetUserId AND m.status = :matchStatus")
    boolean existsByUser2AndStatus(@Param("targetUserId") Long targetUserId,
            @Param("matchStatus") MatchStatus matchStatus);

    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE (m.user1.id = :userId OR m.user2.id = :userId) AND m.status = :status")
    boolean existsAcceptedMatchForUser(@Param("userId") Long userId, @Param("status") MatchStatus status);

    // 스왑 승인 시 사용: 특정 유저의 ACCEPTED 매칭 조회
    @Query("SELECT m FROM Match m WHERE (m.user1 = :user OR m.user2 = :user) AND m.status = :status")
    List<Match> findByUserAndStatus(@Param("user") UserEntity user, @Param("status") MatchStatus status);
}