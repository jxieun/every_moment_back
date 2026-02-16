CREATE TABLE IF NOT EXISTS match_scores (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  match_id BIGINT NOT NULL,  -- 매칭 ID
  user1_score INT NOT NULL DEFAULT 0,  -- user1 점수, 기본값 0
  user2_score INT NOT NULL DEFAULT 0,  -- user2 점수, 기본값 0
  similarity_score DOUBLE NOT NULL DEFAULT 0.0,  -- 유사도 점수, 기본값 0
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 생성 시간
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- 갱신 시간
  CONSTRAINT fk_match_scores_match FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE,  -- 매칭 ID와 연관
  INDEX idx_match_scores_match_id (match_id)  -- 인덱스 추가
) ENGINE=InnoDB;