package com.rookies4.every_moment.match.service;

import com.rookies4.every_moment.match.entity.Match;
import com.rookies4.every_moment.match.entity.MatchResult;
import com.rookies4.every_moment.match.entity.MatchStatus;
import com.rookies4.every_moment.match.entity.SurveyResult;
import com.rookies4.every_moment.match.entity.dto.MatchResultDTO;
import com.rookies4.every_moment.match.repository.MatchRepository;
import com.rookies4.every_moment.match.repository.MatchResultRepository;
import com.rookies4.every_moment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchResultService {

    private final MatchResultRepository matchResultRepository;
    private final UserRepository userRepository;
    private final SurveyService surveyService;
    private final MatchScorerService matchScorerService;
    private final MatchRepository matchRepository;

    // =========================
    // ✅ 관리자 전용: 전체 최신 결과 (페어별 최신 1건)
    // =========================
    public List<MatchResultDTO> getAllCurrentForAdmin() {
        return matchResultRepository.findAllCurrent().stream()
                .map(this::toDtoFromEntity)
                .toList();
    }

    // 나의 매칭 상태 확인 (matchId, status만 반환)
    public List<MatchResultDTO> getSelfMatchResult(Long userId) {
        List<Match> matches = matchRepository.findByUser1_IdOrUser2_Id(userId);

        if (matches.isEmpty()) {
            throw new IllegalArgumentException("매칭을 찾을 수 없습니다.");
        }

        return matches.stream()
                .map(m -> MatchResultDTO.builder()
                        .matchId(String.valueOf(m.getId()))
                        .status(safeStatus(m.getStatus()))
                        .build())
                .collect(Collectors.toList());
    }

    // 자신과 상대방 매칭 상태 확인 (여러 결과 반환)
    @Transactional
    public List<MatchResultDTO> getMatchStatusResult(Long userId, Long matchUserId) {
        List<Match> matches = matchRepository.findByUser1IdAndUser2Id(userId, matchUserId);

        if (matches.isEmpty()) {
            throw new IllegalArgumentException("매칭을 찾을 수 없습니다.");
        }

        return matches.stream()
                .map(m -> MatchResultDTO.builder()
                        .id(m.getId())
                        .matchId(String.valueOf(m.getId()))
                        .status(safeStatus(m.getStatus()))
                        .build())
                .collect(Collectors.toList());
    }

    // 매칭 결과 DTO 생성 후 반환 (저장 포함)
    @Transactional
    public MatchResultDTO getMatchResult(Long userId, Long matchUserId) {
        // 1. 이미 저장된 매칭 결과가 있는지 확인
        Optional<MatchResult> existingResult = matchResultRepository.findByUser_IdAndMatchUser_Id(userId, matchUserId);

        if (existingResult.isPresent()) {
            // 2. 기존 결과가 있다면, 새로운 저장 없이 바로 DTO로 변환하여 반환
            return toDtoFromEntity(existingResult.get());
        }

        // 3. 기존 결과가 없다면, 새로운 결과를 저장
        try {
            SurveyResult userSurveyResult = surveyService.getSurveyResult(userId);
            SurveyResult matchUserSurveyResult = surveyService.getSurveyResult(matchUserId);

            // 점수와 매칭 이유를 계산합니다.
            MatchResultDTO calculatedResult = generateMatchReasons(userSurveyResult, matchUserSurveyResult);

            // 임시 룸 배정 정보
            String roomAssignment = "방 번호를 입력해주세요";
            String roommateName = "익명";

            // 매칭 결과 저장
            MatchResult matchResult = saveMatchResult(userId, matchUserId, calculatedResult.getPreferenceScore(),
                    roomAssignment, roommateName, calculatedResult.getMatchReasons());

            // 최종 DTO 반환
            return toDtoFromEntity(matchResult);

        } catch (IllegalArgumentException e) {
            // 설문 결과가 없는 경우 500 에러 대신 기본값 반환
            return MatchResultDTO.builder()
                    .userId(userId)
                    .matchUserId(matchUserId)
                    .preferenceScore(0.0)
                    .matchReasons(Collections.singletonList("아직 설문이 완료되지 않아 분석할 수 없습니다."))
                    .status("MATCHED")
                    .roomAssignment("미배정")
                    .roommateName("알 수 없음")
                    .build();
        }
    }

    // 매칭이유 상위 3개 생성 및 상위 3개 평균 결과값 반환(100점만점)
    public MatchResultDTO generateMatchReasons(SurveyResult userSurveyResult, SurveyResult matchUserSurveyResult) {
        // 내부 클래스 `SimilarityResult`를 사용하여 점수와 이유를 묶어서 관리
        class SimilarityResult {
            private final BigDecimal score;
            private final String reason;

            public SimilarityResult(BigDecimal score, String reason) {
                this.score = score;
                this.reason = reason;
            }

            public BigDecimal getScore() {
                return score;
            }

            public String getReason() {
                return reason;
            }
        }

        List<SimilarityResult> allSimilarities = new ArrayList<>();

        // 각 유사도 점수 계산 및 반올림
        allSimilarities.add(new SimilarityResult(
                calculateSimilarity(userSurveyResult.getSleepTime(), matchUserSurveyResult.getSleepTime(), 3.0),
                "취침/기상 유사"));
        allSimilarities.add(new SimilarityResult(
                calculateSimilarity(userSurveyResult.getCleanliness(), matchUserSurveyResult.getCleanliness(), 4.0),
                "청결도 유사"));
        allSimilarities.add(new SimilarityResult(
                calculateSimilarity(userSurveyResult.getNoiseSensitivity(), matchUserSurveyResult.getNoiseSensitivity(),
                        3.0),
                "소음 민감도 차이"));
        allSimilarities.add(new SimilarityResult(
                calculateSimilarity(userSurveyResult.getHeight(), matchUserSurveyResult.getHeight(), 3.0),
                "키 유사"));
        allSimilarities.add(new SimilarityResult(
                calculateSimilarity(userSurveyResult.getRoomTemp(), matchUserSurveyResult.getRoomTemp(), 3.0),
                "방 온도 유사"));

        // 점수를 기준으로 내림차순 정렬하고 상위 3개 선택
        List<SimilarityResult> top3Similarities = allSimilarities.stream()
                .sorted(Comparator.comparing(SimilarityResult::getScore).reversed())
                .limit(3)
                .collect(Collectors.toList());

        // 상위 3개 점수의 평균 계산 (100점 만점으로 변환)
        BigDecimal sum = top3Similarities.stream()
                .map(SimilarityResult::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal finalScore = sum.divide(new BigDecimal("3"), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);

        // 상위 3개 이유 목록 생성
        List<String> matchReasons = top3Similarities.stream()
                .map(sim -> sim.getReason() + ": " + sim.getScore().toPlainString())
                .collect(Collectors.toList());

        // 최종 결과 DTO 반환
        return MatchResultDTO.builder()
                .preferenceScore(finalScore.doubleValue())
                .matchReasons(matchReasons)
                .build();
    }

    // MatchResult 객체를 DB에 저장
    private MatchResult saveMatchResult(Long userId, Long matchUserId, double score, String roomAssignment,
            String roommateName, List<String> matchReasons) {
        List<Match> matches = matchRepository.findByUser1IdAndUser2Id(userId, matchUserId);
        if (matches.isEmpty()) {
            throw new IllegalArgumentException("매칭을 찾을 수 없습니다.");
        }

        Match match = matches.get(0);

        MatchResult matchResult = new MatchResult();
        matchResult.setMatch(match);
        matchResult.setUser(userRepository.findById(userId).orElse(null));
        matchResult.setMatchUser(userRepository.findById(matchUserId).orElse(null));
        matchResult.setScore((int) score);
        matchResult.setRoomAssignment(roomAssignment);
        matchResult.setRoommateName(roommateName);
        matchResult.setMatchReasons(matchReasons);
        matchResult.setStatus(match.getStatus());

        return matchResultRepository.save(matchResult);
    }

    // ===== 내부 헬퍼 =====
    private MatchResultDTO toDtoFromEntity(MatchResult m) {
        return MatchResultDTO.builder()
                .id(m.getId())
                .roomAssignment(m.getRoomAssignment())
                .roommateName(safeName(m.getRoommateName()))
                .preferenceScore(m.getScore() != null ? m.getScore().doubleValue() : null)
                .matchReasons(m.getMatchReasons() != null ? m.getMatchReasons() : Collections.emptyList())
                .matchId(m.getMatch() != null ? String.valueOf(m.getMatch().getId()) : "UNKNOWN")
                .status(safeStatus(m.getStatus()))
                .userId(m.getUser() != null ? m.getUser().getId() : null)
                .matchUserId(m.getMatchUser() != null ? m.getMatchUser().getId() : null)
                .userName(m.getUser() != null ? safeName(m.getUser().getUsername()) : null)
                .matchUserName(m.getMatchUser() != null ? safeName(m.getMatchUser().getUsername()) : null)
                .build();
    }

    private BigDecimal calculateSimilarity(Integer userValue, Integer matchUserValue, double scale) {
        if (userValue == null || matchUserValue == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(1 - Math.abs(userValue - matchUserValue) / scale)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String safeName(String s) {
        return (s != null && !s.isBlank()) ? s : "익명";
    }

    private String safeStatus(MatchStatus status) {
        return status != null ? status.name() : "PENDING";
    }
}