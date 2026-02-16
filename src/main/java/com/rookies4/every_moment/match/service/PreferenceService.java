package com.rookies4.every_moment.match.service;

import com.rookies4.every_moment.match.controller.dto.PreferenceResponseDTO;
import com.rookies4.every_moment.match.entity.Preference;
import com.rookies4.every_moment.match.entity.SurveyResult;
import com.rookies4.every_moment.match.repository.PreferenceRepository;
import com.rookies4.every_moment.match.repository.SurveyResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PreferenceService {

    private final PreferenceRepository preferencesRepository; // PreferencesRepository
    private final SurveyResultRepository surveyResultRepository; // SurveyResultRepository

    // 가중치 상수들
    public static final int SLEEP_TIME_WEIGHT = 40;
    public static final int CLEANLINESS_WEIGHT = 18;
    public static final int NOISE_SENSITIVITY_WEIGHT = 18;
    public static final int HEIGHT_WEIGHT = 10;
    public static final int ROOM_TEMP_WEIGHT = 14;

    // 설문 결과를 바탕으로 선호도 계산하여 Preferences 테이블에 저장
    public PreferenceResponseDTO calculateAndSavePreferences(Long userId) {
        // 사용자의 설문 결과 가져오기
        SurveyResult surveyResult = surveyResultRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("설문 결과를 찾을 수 없습니다."));

        // 해당 사용자의 선호도가 이미 존재하는지 확인
        Preference existingPreference = preferencesRepository.findByUserId(userId);

        // 설문 결과를 바탕으로 매칭 점수 계산
        double sleepTimeScore = calculateScore(surveyResult.getSleepTime(), surveyResult.getSleepTime(), SLEEP_TIME_WEIGHT);
        double cleanlinessScore = calculateScore(surveyResult.getCleanliness(), surveyResult.getCleanliness(), CLEANLINESS_WEIGHT);
        double noiseSensitivityScore = calculateNoiseSensitivityScore(surveyResult.getNoiseSensitivity(), surveyResult.getNoiseSensitivity(), NOISE_SENSITIVITY_WEIGHT);
        double heightScore = calculateScore(surveyResult.getHeight(), surveyResult.getHeight(), HEIGHT_WEIGHT);
        double roomTempScore = calculateScore(surveyResult.getRoomTemp(), surveyResult.getRoomTemp(), ROOM_TEMP_WEIGHT);

        Preference preference;

        if (existingPreference != null) {
            // 기존 선호도가 있으면 업데이트
            existingPreference.setSleepTime((int) sleepTimeScore);
            existingPreference.setCleanliness((int) cleanlinessScore);
            existingPreference.setNoiseSensitivity((int) noiseSensitivityScore);
            existingPreference.setHeight((int) heightScore);
            existingPreference.setRoomTemp((int) roomTempScore);

            preference = preferencesRepository.save(existingPreference);
        } else {
            // 선호도가 없으면 새로 생성하여 저장
            preference = new Preference();
            preference.setUser(surveyResult.getUser());  // 설문 결과에서 사용자 가져오기
            preference.setSleepTime((int) sleepTimeScore);
            preference.setCleanliness((int) cleanlinessScore);
            preference.setNoiseSensitivity((int) noiseSensitivityScore);
            preference.setHeight((int) heightScore);
            preference.setRoomTemp((int) roomTempScore);

            // Preference 테이블에 저장
            preference = preferencesRepository.save(preference);
        }

        // PreferenceResponseDTO로 변환하여 반환
        return new PreferenceResponseDTO(
                preference.getId(),
                preference.getUser().getId(),
                preference.getCleanliness(),
                preference.getHeight(),
                preference.getNoiseSensitivity(),
                preference.getRoomTemp(),
                preference.getSleepTime()
        );
    }

    // 점수 계산 메소드: 차이와 가중치 기반으로 점수 계산
    private double calculateScore(int userValue, int matchValue, int weight) {
        int diff = Math.abs(userValue - matchValue);
        return (5 - diff) * weight; // 차이가 적을수록 점수 증가, 최대 차이는 5로 설정
    }

    // 소음 민감도 점수 계산
    private double calculateNoiseSensitivityScore(int userNoiseSensitivity, int matchUserNoiseSensitivity, int weight) {
        double score = 0;

        // 두 사람이 모두 예민한 경우
        if (userNoiseSensitivity == 1 && matchUserNoiseSensitivity == 1) {
            score += weight; // 가점
        }
        // 한 사람만 예민한 경우
        else if (userNoiseSensitivity == 1 || matchUserNoiseSensitivity == 1) {
            score -= weight / 2; // 감점
        }
        // 둘 다 민감하지 않은 경우
        else if (userNoiseSensitivity == 3 && matchUserNoiseSensitivity == 3) {
            score += weight; // 가점
        }

        return score;
    }
    // 선호도 조회
    public PreferenceResponseDTO getPreferences(Long userId) {
        Preference preference = preferencesRepository.findByUserId(userId);

        if (preference != null) {
            // PreferenceResponseDTO로 변환하여 반환
            return new PreferenceResponseDTO(
                    preference.getId(),
                    preference.getUser().getId(),  // userId
                    preference.getCleanliness(),
                    preference.getHeight(),
                    preference.getNoiseSensitivity(),
                    preference.getRoomTemp(),
                    preference.getSleepTime()
            );
        } else {
            throw new IllegalArgumentException("선호도를 찾을 수 없습니다.");
        }
    }

    // 설문결과가 저장된 후 선호도를 자동으로 계산하고 저장하는 로직 추가
    public void savePreferencesForSurvey(Long userId) {
        // 설문결과 저장 후 자동으로 선호도 계산하여 저장
        calculateAndSavePreferences(userId);
    }
}