-- match_recs 테이블 생성
CREATE TABLE match_recs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,  -- 외래키로 UserEntity의 id와 연결
    username VARCHAR(255) NOT NULL,  -- 추천된 사용자 이름 (익명으로 표시)
    score INT NOT NULL,  -- 매칭 점수
    status VARCHAR(255) NOT NULL,  -- 매칭 상태 (PENDING, ACCEPTED, REJECTED, SWAP_REQUESTED)
    roommate_name VARCHAR(255) NOT NULL,  -- 룸메이트 이름 (익명으로 표시)
    preference_score DOUBLE NOT NULL,  -- 룸메이트 선호도 (0~100 범위)
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),  -- 생성일 (자동으로 처리됨)
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),  -- 수정일 (자동으로 처리됨)
    FOREIGN KEY (user_id) REFERENCES users(id)  -- UserEntity와 외래키 관계 설정 (users 테이블의 id와 연결)
);
