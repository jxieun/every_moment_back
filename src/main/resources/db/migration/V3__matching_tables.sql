-- =====================================================================
-- V3__matching_tables.sql
-- 매칭/설문 관련 테이블 정의 (MySQL) + 매칭 테이블 수정 (점수 및 유사도 추가)
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

-- =====================================================================
-- 1) preferences 테이블 ([User 1:1] 설문선호도)
-- =====================================================================
CREATE TABLE IF NOT EXISTS preferences (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  sleep_time INT NOT NULL,
  cleanliness INT NOT NULL,
  noise_sensitivity INT NOT NULL,
  height INT NOT NULL,
  room_temp INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 생성 시 현재 시간 자동 입력
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- 수정 시 현재 시간 자동 업데이트
  CONSTRAINT fk_preference_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT uk_preference_user UNIQUE (user_id)  -- 1:1 관계 보장
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- 2) matches 테이블 (매칭 점수 및 상태)
-- =====================================================================
CREATE TABLE IF NOT EXISTS matches (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user1_id BIGINT NOT NULL,
  user2_id BIGINT NOT NULL,
  status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'SWAP_REQUESTED') NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  user1_score INT NOT NULL DEFAULT 0,  -- user1의 점수 추가
  user2_score INT NOT NULL DEFAULT 0,  -- user2의 점수 추가
  similarity_score DOUBLE,  -- 유사도 점수 추가
  CONSTRAINT fk_matches_user1 FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_matches_user2 FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_matches_pair ON matches(user1_id, user2_id);
CREATE INDEX idx_matches_status ON matches(status);

-- =====================================================================
-- 3) match_results 테이블 (매칭 결과)
-- =====================================================================
CREATE TABLE IF NOT EXISTS match_results (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  match_user_id BIGINT NOT NULL,
  score INT NOT NULL,
  room_assignment VARCHAR(255) NOT NULL,
  roommate_name VARCHAR(255) NOT NULL,
  status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'SWAP_REQUESTED') NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_match_result_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_match_result_match_user FOREIGN KEY (match_user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_match_results_user ON match_results(user_id);
CREATE INDEX idx_match_results_pair ON match_results(user_id, match_user_id);
CREATE INDEX idx_match_results_status ON match_results(status);

-- =====================================================================
-- 4) match_result_reasons 테이블 (매칭 이유)
-- =====================================================================
CREATE TABLE IF NOT EXISTS match_result_reasons (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  match_result_id BIGINT NOT NULL,
  reason VARCHAR(255) NOT NULL,
  CONSTRAINT fk_mr_reasons FOREIGN KEY (match_result_id) REFERENCES match_results(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_mr_reasons_mrid ON match_result_reasons(match_result_id);

-- =====================================================================
-- 5) survey_results 테이블 (사용자 설문 결과)
-- =====================================================================
CREATE TABLE IF NOT EXISTS survey_results (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  sleep_time INT NOT NULL,
  cleanliness INT NOT NULL,
  noise_sensitivity INT NOT NULL,
  height INT NOT NULL,
  room_temp INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_survey_result_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_survey_results_user ON survey_results(user_id);

SET FOREIGN_KEY_CHECKS=1;