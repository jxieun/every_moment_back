package com.rookies4.every_moment.match.service;

import com.rookies4.every_moment.match.entity.*;
import com.rookies4.every_moment.match.entity.dto.MatchProposalDTO;
import com.rookies4.every_moment.entity.UserEntity;
import com.rookies4.every_moment.match.repository.MatchRepository;
import com.rookies4.every_moment.match.repository.MatchScoresRepository;
import com.rookies4.every_moment.repository.UserRepository;
import com.rookies4.every_moment.match.repository.SurveyResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final MatchScorerService matchScorerService;
    private final SurveyResultRepository surveyResultRepository;
    private final MatchScoresRepository matchScoresRepository;

    // 점수 계산 및 저장을 위한 별도 메서드
    private MatchScores calculateAndSaveMatchScores(Match match, SurveyResult user1Survey, SurveyResult user2Survey) {
        // 각 사용자의 점수 계산
        int user1Score = (int) matchScorerService.calculateScore(user1Survey, user2Survey); // user1과 user2 비교
        int user2Score = (int) matchScorerService.calculateScore(user2Survey, user1Survey); // user2와 user1 비교

        // 유사도 계산
        double similarityScore = matchScorerService.calculateSimilarity(user1Survey, user2Survey);

        // MatchScores 객체 생성하여 계산된 점수들을 설정
        MatchScores matchScores = new MatchScores();
        matchScores.setMatch(match); // 매칭과 연관시킴
        matchScores.setUser1_Score(user1Score); // user1 점수 설정
        matchScores.setUser2_Score(user2Score); // user2 점수 설정
        matchScores.setSimilarityScore(similarityScore); // 유사도 점수 설정

        // MatchScores 저장
        matchScoresRepository.save(matchScores);

        // 저장된 MatchScores 객체 반환
        return matchScores;
    }

    private void calculateAndSetMatchScores(Match match, SurveyResult user1Survey, SurveyResult user2Survey) {
        int user1Score = (int) matchScorerService.calculateScore(user1Survey, user2Survey);
        int user2Score = (int) matchScorerService.calculateScore(user2Survey, user1Survey);
        double similarityScore = matchScorerService.calculateSimilarity(user1Survey, user2Survey);

        match.setUser1_Score(user1Score);
        match.setUser2_Score(user2Score);
        match.setSimilarityScore(similarityScore);
    }

    // 매칭 제안
    @Transactional
    public Long proposeMatch(Long userId, MatchProposalDTO proposal) {
        UserEntity proposer = userRepository.findById(proposal.getProposerId())
                .orElseThrow(() -> new IllegalArgumentException("제안자를 찾을 수 없습니다."));
        UserEntity targetUser = userRepository.findById(proposal.getTargetUserId())
                .orElseThrow(() -> new IllegalArgumentException("대상 사용자를 찾을 수 없습니다."));

        // 제안자가 이미 ACCEPTED 상태로 매칭되었는지 확인 (자신이 이미 매칭을 수락한 경우 제안 불가)
        if (matchRepository.existsByUserAndStatus(proposer.getId(), MatchStatus.ACCEPTED)) {
            throw new IllegalArgumentException(proposer.getUsername() + "는 이미 다른 사용자와 매칭을 수락한 상태입니다. 제안이 불가능합니다.");
        }

        // 대상 사용자가 이미 ACCEPTED 상태로 매칭되었는지 확인 (대상 사용자가 매칭을 수락한 경우 제안 불가)
        if (matchRepository.existsByUserAndStatus(targetUser.getId(), MatchStatus.ACCEPTED)) {
            throw new IllegalArgumentException(targetUser.getUsername() + "은 이미 다른 사용자와 매칭을 수락했습니다. 제안이 불가능합니다.");
        }

        // 이미 두 사용자 사이(PENDING) 매칭 존재하면 예외 발생
        if (matchRepository.existsPendingMatchBetweenUsers(proposer.getId(), targetUser.getId(), MatchStatus.PENDING)) {
            throw new IllegalArgumentException(
                    "이미 " + proposer.getUsername() + "와 " + targetUser.getUsername() + " 간에는 제안된 매칭이 있습니다.");
        }

        SurveyResult user1Survey = surveyResultRepository.findByUserId(proposer.getId())
                .orElseGet(() -> createDefaultSurveyResult(proposer));
        SurveyResult user2Survey = surveyResultRepository.findByUserId(targetUser.getId())
                .orElseGet(() -> createDefaultSurveyResult(targetUser));

        Match match = new Match();
        match.setUser1(proposer);
        match.setUser2(targetUser);
        match.setStatus(MatchStatus.PENDING);

        // 점수를 계산하여 매칭 객체에 바로 설정
        calculateAndSetMatchScores(match, user1Survey, user2Survey);

        // 매칭 객체를 먼저 저장하여 ID를 할당받음
        Match savedMatch = matchRepository.save(match);

        // MatchScores 객체를 생성하고 저장
        MatchScores matchScores = new MatchScores();
        matchScores.setMatch(savedMatch);
        matchScores.setUser1_Score(savedMatch.getUser1_Score());
        matchScores.setUser2_Score(savedMatch.getUser2_Score());
        matchScores.setSimilarityScore(savedMatch.getSimilarityScore());

        matchScoresRepository.save(matchScores);

        return savedMatch.getId();
    }

    // 매칭 수락
    @Transactional
    public void acceptMatch(Long matchId) {
        Optional<Match> matchOptional = matchRepository.findById(matchId);

        if (matchOptional.isPresent()) {
            Match match = matchOptional.get();

            // 매칭이 이미 수락된 상태인지 확인
            if (match.getStatus().equals(MatchStatus.ACCEPTED)) {
                throw new IllegalArgumentException("이미 수락된 매칭입니다.");
            }

            if (matchRepository.existsAcceptedMatchForUser(match.getUser1().getId(), MatchStatus.ACCEPTED) ||
                    matchRepository.existsAcceptedMatchForUser(match.getUser2().getId(), MatchStatus.ACCEPTED)) {
                throw new IllegalArgumentException("매칭된 사용자는 이미 다른 매칭을 수락했습니다.");
            }

            // 현재 사용자가 이미 수락한 매칭이 있는지 확인 (user1이 수락된 매칭을 가지고 있으면, 수락 불가)
            if (matchRepository.existsByUser1AndStatus(match.getUser1().getId(), MatchStatus.ACCEPTED)) {
                throw new IllegalArgumentException("현재 사용자는 이미 수락된 매칭을 가지고 있어 다른 매칭을 수락할 수 없습니다.");
            }

            // 매칭 상태가 PENDING인 경우에만 로직 진행
            if (match.getStatus().equals(MatchStatus.PENDING)) {
                // 매칭 상태를 ACCEPTED로 변경
                match.setStatus(MatchStatus.ACCEPTED);
                matchRepository.save(match);
            } else {
                throw new IllegalArgumentException("매칭을 찾을 수 없거나 이미 수락된 상태입니다.");
            }
        } else {
            throw new IllegalArgumentException("매칭을 찾을 수 없습니다.");
        }
    }

    // 매칭 거절 처리
    @Transactional
    public void rejectMatch(Long matchId) {
        Optional<Match> matchOptional = matchRepository.findById(matchId);

        if (matchOptional.isPresent()) {
            Match match = matchOptional.get();
            if (MatchStatus.PENDING.equals(match.getStatus())) {
                match.setStatus(MatchStatus.REJECTED);
                matchRepository.save(match);
            } else {
                throw new IllegalArgumentException("이미 거절된 매칭입니다.");
            }
        } else {
            throw new IllegalArgumentException("매칭을 찾을 수 없습니다.");
        }
    }

    // 새로운 매칭을 만드는 대신, 거절된 매칭의 유저 ID를 반환
    @Transactional(readOnly = true) // 데이터 변경이 없으므로 읽기 전용으로 설정
    public List<Long> getRejectedMatchUserIds(Long matchId) {
        Optional<Match> matchOptional = matchRepository.findById(matchId);

        if (matchOptional.isPresent()) {
            Match match = matchOptional.get();

            if (MatchStatus.REJECTED.equals(match.getStatus())) {
                // 거절된 매칭의 user1과 user2의 ID를 리스트로 반환
                List<Long> userIds = new ArrayList<>();
                userIds.add(match.getUser1().getId());
                userIds.add(match.getUser2().getId());
                return userIds;
            } else {
                throw new IllegalArgumentException("거절된 매칭이 아닙니다.");
            }
        } else {
            throw new IllegalArgumentException("매칭을 찾을 수 없습니다.");
        }
    }

    // 스왑 신청 처리 (새로운 매칭 상태인 SWAP_REQUESTED로 변경)
    @Transactional
    public void swapMatch(Long matchId) {
        Optional<Match> matchOptional = matchRepository.findById(matchId);

        if (matchOptional.isPresent()) {
            Match match = matchOptional.get();
            if (MatchStatus.PENDING.equals(match.getStatus())) {
                match.setStatus(MatchStatus.SWAP_REQUESTED);
                matchRepository.save(match);
                proposeNewMatch(match.getUser1().getId(), match.getUser2().getId());
            } else {
                throw new IllegalArgumentException("매칭 상태가 PENDING이 아니면 스왑을 신청할 수 없습니다.");
            }
        } else {
            throw new IllegalArgumentException("매칭을 찾을 수 없습니다.");
        }
    }

    // 관리자가 새로운 매칭을 제안할 수 있도록 하는 메소드
    @Transactional
    public Long proposeNewMatch(Long proposerId, Long targetUserId) {
        UserEntity proposer = userRepository.findById(proposerId)
                .orElseThrow(() -> new IllegalArgumentException("제안자를 찾을 수 없습니다."));
        UserEntity targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("대상 사용자를 찾을 수 없습니다."));

        List<Match> existingMatches = matchRepository.findByUser1_IdAndUser2_Id(proposer.getId(), targetUser.getId());

        for (Match match : existingMatches) {
            if (MatchStatus.SWAP_REQUESTED.equals(match.getStatus())) {
                SurveyResult user1Survey = surveyResultRepository.findByUserId(proposer.getId())
                        .orElseThrow(() -> new IllegalArgumentException("사용자 1의 설문 결과가 없습니다."));
                SurveyResult user2Survey = surveyResultRepository.findByUserId(targetUser.getId())
                        .orElseThrow(() -> new IllegalArgumentException("사용자 2의 설문 결과가 없습니다."));

                Match newMatch = new Match();
                newMatch.setUser1(proposer);
                newMatch.setUser2(targetUser);
                newMatch.setStatus(MatchStatus.PENDING);

                calculateAndSetMatchScores(newMatch, user1Survey, user2Survey);

                // 1. 매칭 객체를 먼저 저장하여 ID를 할당받음
                Match savedMatch = matchRepository.save(newMatch);

                // 2. MatchScores 객체를 생성하고, 방금 저장된 Match 객체(savedMatch)를 연결
                MatchScores matchScores = new MatchScores();
                matchScores.setMatch(savedMatch);
                matchScores.setUser1_Score(savedMatch.getUser1_Score());
                matchScores.setUser2_Score(savedMatch.getUser2_Score());
                matchScores.setSimilarityScore(savedMatch.getSimilarityScore());

                // 3. MatchScores를 저장
                matchScoresRepository.save(matchScores);

                return savedMatch.getId();
            }
        }

        throw new IllegalArgumentException("스왑 신청이 완료된 매칭만 새로운 매칭을 제안할 수 있습니다.");
    }

    private SurveyResult createDefaultSurveyResult(UserEntity user) {
        SurveyResult defaultSurvey = new SurveyResult();
        defaultSurvey.setUser(user);
        defaultSurvey.setSleepTime(0);
        defaultSurvey.setCleanliness(0);
        defaultSurvey.setNoiseSensitivity(0);
        defaultSurvey.setHeight(0);
        defaultSurvey.setRoomTemp(0);
        return defaultSurvey;
    }
}