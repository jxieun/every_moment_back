package com.rookies4.every_moment.match.repository;

import com.rookies4.every_moment.match.entity.SurveyResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyResultRepository extends JpaRepository<SurveyResult, Long> {
    Optional<SurveyResult> findByUserId(Long userId);

    // 주어진 사용자 ID 목록에 대한 설문 결과를 반환하는 메서드
    List<SurveyResult> findAllByUserIdIn(List<Long> userIds);
}