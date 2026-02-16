package com.rookies4.every_moment.match.service;

import com.rookies4.every_moment.match.entity.Match;
import com.rookies4.every_moment.match.entity.dto.MatchResultDTO;
import com.rookies4.every_moment.match.entity.MatchResult;
import com.rookies4.every_moment.match.entity.MatchStatus;
import com.rookies4.every_moment.match.entity.SurveyResult;
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

    // 나의 매칭 상태 확인 (matchId, status만 반환)
    public List<MatchResultDTO> getSelfMatchResult(Long userId) {
        // 사용자와 관련된 모든 매칭을 가져옴
        List<Match> matches = matchRepository.findByUser1_IdOrUser2_Id(userId);  // user1_id 또는 user2_id가 userId인 모든 매칭을 가져옴

        // 매칭 결과가 존재하는 경우
        if (!matches.isEmpty()) {
            List<MatchResultDTO> matchResultDTOList = new ArrayList<>();

            // 매칭 상태별로 처리
            for (Match match : matches) {
                String status = match.getStatus().name();  // 매칭 상태
                Long matchId = match.getId();  // 매칭 ID

                // 상태에 따라 DTO 생성
                matchResultDTOList.add(new MatchResultDTO(
                        matchId,  // 매칭 ID
                        null,  // 룸 배정은 null로 처리
                        null,  // 룸메이트 이름은 null로 처리
                        null,  // 선호도 점수는 null로 처리
                        null,  // 매칭 이유는 null로 처리
                        String.valueOf(matchId),  // 매칭 ID를 문자열로 반환
                        status  // 상태
                ));
            }
            return matchResultDTOList;
        } else {
            throw new IllegalArgumentException("매칭을 찾을 수 없습니다.");
        }
    }

    // 자신과 상대방 매칭 상태 확인 (여러 결과 반환)
    @Transactional
    public List<MatchResultDTO> getMatchStatusResult(Long userId, Long matchUserId) {
        // 두 사용자가 매칭된 Match 데이터를 가져옴
        List<Match> matches = matchRepository.findByUser1IdAndUser2Id(userId, matchUserId);

        // 결과가 존재하는 경우
        if (!matches.isEmpty()) {
            List<MatchResultDTO> matchResultDTOList = new ArrayList<>();

            // 매칭 상태에 따라 처리
            for (Match match : matches) {
                // 상태와 매칭 ID만 반환 (PENDING, REJECTED, SWAP_REQUESTED, ACCEPTED 등)
                matchResultDTOList.add(new MatchResultDTO(
                        match.getId(),  // 룸 배정은 null로 처리
                        null,
                        null,  // 룸메이트 이름도 null로 처리
                        null,  // 선호도 점수는 null로 처리
                        null,
                        match.getId() != null ? String.valueOf(match.getId()) : "UNKNOWN",  // 매칭 ID 가져오기
                        match.getStatus() != null ? match.getStatus().name() : "UNKNOWN"  // Match 엔티티의 상태 가져오기

                ));
            }
            return matchResultDTOList;
        } else {
            throw new IllegalArgumentException("매칭을 찾을 수 없습니다.");
        }
    }

    // 매칭 결과 DTO 생성 후 반환
    public MatchResultDTO getMatchResult(Long userId, Long matchUserId) {
        // 사용자의 설문 결과 조회
        SurveyResult userSurveyResult = surveyService.getSurveyResult(userId);
        SurveyResult matchUserSurveyResult = surveyService.getSurveyResult(matchUserId);

        // 매칭 점수 계산 및 매칭 이유 생성
        MatchResultDTO matchResultDTO = generateMatchReasons(userSurveyResult, matchUserSurveyResult);

        // 룸 배정 정보 (예: "A동 304호 (DOUBLE)" 등)
        String roomAssignment = "A동 304호 (DOUBLE)"; // 예시로 고정 배정 (나중에 로직으로 처리 가능)

        // 룸메이트 이름 (익명으로 표시)
        String roommateName = "익명";

        // 매칭 결과 DB에 저장
        MatchResult matchResult = saveMatchResult(userId, matchUserId, matchResultDTO.getPreferenceScore(), roomAssignment, roommateName, matchResultDTO.getMatchReasons());

        // 상태 가져오기
        String status = matchResult.getMatch().getStatus().name(); // Match 상태 가져오기

        // 매칭 ID 가져오기
        String matchId = String.valueOf(matchResult.getMatch().getId()); // 매칭 ID를 가져옴

        // 매칭 결과 DTO 생성 후 반환 (matchId와 status 포함)
        return new MatchResultDTO(
                matchResult.getId(),
                roomAssignment,
                roommateName,
                matchResultDTO.getPreferenceScore(),  // 매칭 점수
                matchResultDTO.getMatchReasons(),  // 매칭 이유
                matchId,  // matchId를 직접 가져옴
                status   // 상태를 직접 가져옴
        );
    }

    //MatchResult 객체를 DB에 저장
    private MatchResult saveMatchResult(Long userId, Long matchUserId, double score, String roomAssignment, String roommateName, List<String> matchReasons) {
        // 사용자와 매칭된 Match 객체를 찾습니다.
        List<Match> matches = matchRepository.findByUser1IdAndUser2Id(userId, matchUserId);
        if (matches.isEmpty()) {
            throw new IllegalArgumentException("매칭을 찾을 수 없습니다.");
        }

        // matches에서 첫 번째 결과를 사용
        Match match = matches.get(0);  // 첫 번째 매칭 결과 가져오기

        // 새로운 MatchResult 객체 생성
        MatchResult matchResult = new MatchResult();
        matchResult.setMatch(match);  // Match 객체 설정
        matchResult.setUser(userRepository.findById(userId).orElse(null));
        matchResult.setMatchUser(userRepository.findById(matchUserId).orElse(null));  // matchUser 설정 추가
        matchResult.setScore((int) score);
        matchResult.setRoomAssignment(roomAssignment);
        matchResult.setRoommateName(roommateName);
        matchResult.setMatchReasons(matchReasons);

        // Match 상태를 기반으로 MatchResult 상태 설정
        if (match.getStatus() == MatchStatus.ACCEPTED) {
            matchResult.setStatus(MatchStatus.ACCEPTED);
        } else if (match.getStatus() == MatchStatus.REJECTED) {
            matchResult.setStatus(MatchStatus.REJECTED);
        } else {
            matchResult.setStatus(MatchStatus.PENDING);  // 기본 상태는 PENDING
        }

        // MatchResult 객체를 DB에 저장
        return matchResultRepository.save(matchResult);
    }


    //매칭이유 상위 3개 생성 및 상위 3개 평균 결과값 반환(100점만점)
    public MatchResultDTO generateMatchReasons(SurveyResult userSurveyResult, SurveyResult matchUserSurveyResult) {
        // 점수와 이유를 묶어서 관리하는 클래스
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

        // 1. 각 유사도 점수를 계산하고, 소수점 둘째 자리에서 반올림
        BigDecimal sleepTimeSimilarity = new BigDecimal(1 - Math.abs(userSurveyResult.getSleepTime() - matchUserSurveyResult.getSleepTime()) / 3.0).setScale(2, RoundingMode.HALF_UP);
        allSimilarities.add(new SimilarityResult(sleepTimeSimilarity, "취침/기상 유사: " + sleepTimeSimilarity.toPlainString()));

        BigDecimal cleanlinessSimilarity = new BigDecimal(1 - Math.abs(userSurveyResult.getCleanliness() - matchUserSurveyResult.getCleanliness()) / 4.0).setScale(2, RoundingMode.HALF_UP);
        allSimilarities.add(new SimilarityResult(cleanlinessSimilarity, "청결도 유사: " + cleanlinessSimilarity.toPlainString()));

        BigDecimal noiseSensitivitySimilarity = new BigDecimal(1 - Math.abs(userSurveyResult.getNoiseSensitivity() - matchUserSurveyResult.getNoiseSensitivity()) / 3.0).setScale(2, RoundingMode.HALF_UP);
        allSimilarities.add(new SimilarityResult(noiseSensitivitySimilarity, "소음 민감도 차이: " + noiseSensitivitySimilarity.toPlainString()));

        BigDecimal heightSimilarity = new BigDecimal(1 - Math.abs(userSurveyResult.getHeight() - matchUserSurveyResult.getHeight()) / 3.0).setScale(2, RoundingMode.HALF_UP);
        allSimilarities.add(new SimilarityResult(heightSimilarity, "층고 유사: " + heightSimilarity.toPlainString()));

        BigDecimal roomTempSimilarity = new BigDecimal(1 - Math.abs(userSurveyResult.getRoomTemp() - matchUserSurveyResult.getRoomTemp()) / 3.0).setScale(2, RoundingMode.HALF_UP);
        allSimilarities.add(new SimilarityResult(roomTempSimilarity, "방 온도 유사: " + roomTempSimilarity.toPlainString()));

        // 2. 반올림된 점수를 내림차순 정렬
        allSimilarities.sort(Comparator.comparing(SimilarityResult::getScore).reversed());

        // 3. 상위 3개 항목만 선택
        List<SimilarityResult> top3Similarities = allSimilarities.stream()
                .limit(3)
                .collect(Collectors.toList());

        // 4. 반올림된 상위 3개 점수를 사용해 최종 점수 계산
        BigDecimal sum = top3Similarities.stream()
                .map(SimilarityResult::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalScore = sum.divide(new BigDecimal("3"), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);

        // 상위 3개 이유만 반환
        List<String> matchReasons = top3Similarities.stream()
                .map(SimilarityResult::getReason)
                .collect(Collectors.toList());

        return new MatchResultDTO(
                null,
                null,
                null,
                finalScore.doubleValue(),
                matchReasons,
                null,
                null
        );
    }
}