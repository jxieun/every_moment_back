package com.rookies4.every_moment.match.controller;

import com.rookies4.every_moment.match.controller.dto.SurveyResultResponseDTO;
import com.rookies4.every_moment.match.entity.SurveyResult;
import com.rookies4.every_moment.match.service.PreferenceService;
import com.rookies4.every_moment.match.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/survey")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;
    private final PreferenceService preferenceService;


    // 설문지 제출 및 선호도 계산 후 저장
    @PostMapping("/submit/{userId}")
    public ResponseEntity<SurveyResult> submitSurvey(@PathVariable Long userId, @RequestBody SurveyResult surveyResult) {
        if (userId == null) {
            // userId가 없는 경우 Unauthorized(401) 반환
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // 1. 설문 결과 저장
            SurveyResult savedSurveyResult = surveyService.submitSurveyResult(userId, surveyResult);

            // 2. 설문 저장 후 선호도 계산 및 저장
            preferenceService.savePreferencesForSurvey(userId);

            // 3. 설문 제출 완료 응답
            return ResponseEntity.status(HttpStatus.CREATED).body(savedSurveyResult);
        } catch (Exception e) {
            // 예외 처리: 설문 제출 실패 시 오류 메시지 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    // 설문 결과 조회
    @GetMapping("/{userId}")
    public ResponseEntity<SurveyResultResponseDTO> getSurveyResult(@PathVariable Long userId) {
        SurveyResult surveyResult = surveyService.getSurveyResult(userId);

        // DTO 변환
        SurveyResultResponseDTO response = new SurveyResultResponseDTO(
                surveyResult.getId(),
                surveyResult.getUser().getId(), // user 정보 사용
                surveyResult.getSleepTime(),
                surveyResult.getCleanliness(),
                surveyResult.getNoiseSensitivity(),
                surveyResult.getHeight(),
                surveyResult.getRoomTemp()
        );

        return ResponseEntity.ok(response);
    }
}
