-- V10: Add sample posts for demo purposes
-- Categories: NOTICE, FREE, MATCH, FIND
-- Status: NORMAL, SWAP_REQUEST, SWAP_APPROVED, SWAP_REJECTED

-- Admin user ID = 1 (assumed from seed data)

-- 공지 게시판 (NOTICE)
INSERT INTO posts (author_id, category, title, content, view_count, deleted, status, created_at, updated_at)
VALUES 
(1, 'NOTICE', '🎉 Every Moment 서비스 오픈 안내', 
'안녕하세요, Every Moment 관리자입니다.

드디어 Every Moment 서비스가 정식 오픈하였습니다! 🎊

**주요 기능:**
✅ 설문 기반 지능형 룸메이트 매칭
✅ 실시간 채팅 및 매칭 결과 확인
✅ 층고 선호도 기반 자동 호실 배정
✅ 자유 게시판 및 커뮤니티 기능

궁금한 점이 있으시면 언제든지 문의 주세요!
행복한 기숙사 생활 되시길 바랍니다 😊', 
150, false, 'NORMAL', NOW(), NOW()),

(1, 'NOTICE', '📋 설문 작성 가이드', 
'룸메이트 매칭을 위해서는 먼저 설문을 완료해주셔야 합니다.

**설문 항목:**
1. 취침/기상 시간
2. 청결도 선호
3. 소음 민감도
4. 층고 선호 (저층/중층/고층)
5. 방 온도 선호

설문 결과는 매칭 알고리즘에 활용되며, 최적의 룸메이트를 찾는 데 도움이 됩니다.
정확하게 작성할수록 만족도 높은 매칭 결과를 받으실 수 있습니다! 💯', 
85, false, 'NORMAL', NOW(), NOW()),

(1, 'NOTICE', '⚠️ 이용 시 주의사항', 
'**서비스 이용 시 다음 사항을 준수해 주시기 바랍니다:**

1. 타인을 비방하거나 불쾌하게 하는 게시글은 삭제될 수 있습니다
2. 매칭 후 룸메이트와 원활한 소통을 위해 채팅을 자주 확인해 주세요
3. 호실 배정은 자동으로 이루어지며, 수정이 필요한 경우 관리자에게 문의하세요
4. 개인정보는 철저히 보호되므로 안심하고 이용하시기 바랍니다

문의사항은 관리자 채팅으로 연락 부탁드립니다 🙏', 
62, false, 'NORMAL', NOW(), NOW());

-- 자유 게시판 (FREE)
INSERT INTO posts (author_id, category, title, content, view_count, deleted, status, created_at, updated_at)
VALUES 
(1, 'FREE', '🍕 기숙사 근처 맛집 추천해주세요!', 
'이번에 입사하게 된 새내기입니다!
기숙사 근처에 괜찮은 음식점 있으면 추천 부탁드려요 😋

특히 야식 배달 잘 되는 곳이나 혼밥하기 좋은 곳 알려주시면 감사하겠습니다!', 
45, false, 'NORMAL', NOW(), NOW()),

(1, 'FREE', '📚 도서관 이용 꿀팁!', 
'학교 도서관 자주 이용하시는 분들께 추천드립니다!

- 3층 창가 자리: 조용하고 뷰가 좋음
- 5층 스터디룸: 그룹 스터디 최적
- 1층 카페: 커피 마시며 가볍게 공부하기 좋음

다들 좋은 공부 환경 찾으시길! 💪', 
38, false, 'NORMAL', NOW(), NOW()),

(1, 'FREE', '🎮 같이 게임하실 분?', 
'저녁 시간에 롤 같이 하실 분 구합니다!
티어는 골드~플레 정도구요, 편하게 즐겜 위주로 하시는 분이면 좋겠습니다 ㅎㅎ

댓글로 연락 주세요~', 
29, false, 'NORMAL', NOW(), NOW());

-- 매칭 게시판 (MATCH)
INSERT INTO posts (author_id, category, title, content, view_count, deleted, status, created_at, updated_at)
VALUES 
(1, 'MATCH', '✨ 매칭 성공 후기!', 
'설문 결과로 매칭됐는데 진짜 잘 맞는 것 같아요!
취침 시간도 비슷하고 청소 스타일도 같아서 편하게 지내고 있습니다 😊

걱정했었는데 Every Moment 덕분에 좋은 룸메이트 만났어요!
아직 고민 중이신 분들 추천드립니다~', 
52, false, 'NORMAL', NOW(), NOW()),

(1, 'MATCH', '🤝 룸메이트와 잘 지내는 법', 
'매칭 후 한 달 지낸 경험 공유합니다!

**Tip:**
1. 처음에 서로 생활 패턴 충분히 이야기하기
2. 불편한 점 있으면 바로 말하기 (쌓이면 힘듦)
3. 청소 당번 정해두기
4. 서로 배려하는 마음 갖기

다들 행복한 기숙사 생활 하세요! 💙', 
41, false, 'NORMAL', NOW(), NOW()),

(1, 'MATCH', '🔄 룸메이트 스왑(교체) 시스템 안내', 
'**룸메이트와 맞지 않아 변경을 원하시나요?**

Every Moment는 스왑 시스템을 통해 룸메이트 재배정을 지원합니다!

**📋 신청 방법:**
1. "룸메이트 찾기" 게시판에 스왑 신청 글 작성
2. 제목에 [스왑 신청] 표시 권장
3. 현재 상황과 원하는 조건 상세히 작성

**⚙️ 처리 과정:**
1. 관리자가 신청 내용 검토
2. 승인 시: 기존 매칭 및 호실 자동 초기화
3. 새로운 설문 기반 매칭 진행
4. 선호 층에 맞는 호실 재배정

**⏰ 소요 시간:**
보통 1~2일 내 검토 완료

**📌 참고사항:**
- 스왑 승인 시 이전 룸메이트도 재매칭 대상이 됩니다
- 거절 시 기존 룸메이트와 계속 지내게 됩니다
- 신중하게 결정해 주세요!

문의사항은 관리자 채팅으로 연락 주세요 🙏', 
95, false, 'NORMAL', NOW(), NOW());

-- 룸메이트 찾기 (FIND)
INSERT INTO posts (author_id, category, title, content, view_count, deleted, status, created_at, updated_at)
VALUES 
(1, 'FIND', '🔍 조용한 환경 선호하는 룸메이트 구해요', 
'**원하는 조건:**
- 야행성 아니신 분 (11시~12시 취침)
- 소음에 예민하신 분
- 청결 중시하시는 분

저도 비슷한 스타일이라 잘 맞을 것 같아요!
설문 결과 확인 후 연락 주세요 😊', 
33, false, 'NORMAL', NOW(), NOW()),

(1, 'FIND', '🌙 올빼미족 룸메이트 찾아요!', 
'새벽까지 과제하거나 공부하는 스타일이에요
비슷하게 늦게 주무시는 분이랑 같이 지내고 싶습니다!

- 새벽 2~3시 취침
- 조명/키보드 소리 괜찮으신 분
- 서로 방해 안 하며 지낼 수 있는 분

연락 주세요~ 🦉', 
27, false, 'NORMAL', NOW(), NOW());
