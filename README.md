# 🕒 Every Moment (모든 순간) Backend

사용자의 가치관 설문을 기반으로 최적의 인연을 연결하고, 실시간 소통과 커뮤니티 기능을 제공하는 Spring Boot 기반 매칭 플랫폼 백엔드 서버입니다.

## 🚀 배포 주소 (Live URLs)
- **Frontend**: [https://everymomentmini2front.vercel.app](https://everymomentmini2front.vercel.app)
- **Backend API**: [https://every-moment-back.onrender.com](https://every-moment-back.onrender.com)
- **Swagger Docs**: [https://every-moment-back.onrender.com/swagger-ui.html](https://every-moment-back.onrender.com/swagger-ui.html)

## 🔑 관리자 및 테스트 계정 (Credentials)
### 👑 관리자 (Admin)
- **Email**: `admin@example.com`
- **Password**: `AdminPassw0rd!`
> **권한**: 채팅방 조회, 사용자 관리 등

### 👤 테스트 유저 (Demo User)
- **Email**: `demo@example.com`
- **Password**: `Passw0rd!`
> **권한**: 일반 사용자 기능 (매칭, 설문 등)

## 🛠️ 주요 이슈 해결 내역 (Troubleshooting)
- **DB Connection**: TiDB 클라우드 연동 및 타임존 설정 완료
- **CORS**: Vercel 프론트엔드 도메인 화이트리스팅 완료
- **Match 500 Error**: `created_at` Null 문제 해결 및 설문 데이터 예외 처리 완료
- **Timezone**: Asia/Seoul(KST) 기본 타임존 설정으로 시간 표기 오류 수정
- **Null Handler**: MatchScorerService Integer null 처리로 NullPointerException 방지
- **Auto Room Assignment**: 설문 층고 선호도 기반 자동 호실 배정 구현 (1층=101~, 2층=201~, 3층=301~)

---

## 📋 목차

- [개요](#개요)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [주요 기능](#주요-기능)
- [API 엔드포인트](#api-엔드포인트)
- [설치 및 실행](#설치-및-실행)
- [데이터베이스 설정](#데이터베이스-설정)

---

## 개요

**모든 순간(Every Moment)**은 단순한 외형적 조건을 넘어, 사용자가 진행한 설문 결과를 알고리즘으로 분석하여 정서적/가치관적 합치도가 높은 상대를 추천합니다. 매칭된 상대와의 실시간 STOMP 채팅과 자유로운 커뮤니티 활동을 통해 진정성 있는 관계 형성을 지원합니다.

## 기술 스택

| 구분 | 기술 |
|------|------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2.5 |
| **Database** | MySQL |
| **ORM** | Spring Data JPA |
| **Security** | Spring Security + JWT |
| **Messaging** | Spring WebSocket (STOMP) |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) |
| **Build Tool** | Maven |
| **Migration** | Flyway |

## 프로젝트 구조

```text
com.rookies4.every_moment/
├── auth/                           # 인증 및 회원가입 비즈니스 로직
├── board/                          # 커뮤니티 게시판 도메인
│   ├── controller/                 # 게시글, 댓글, 활동 로그 컨트롤러
│   ├── entity/                     # PostEntity, CommentEntity, BoardLogEntity
│   ├── repository/                 # JPA 데이터 접근 계층
│   └── service/                    # 소셜 기능 핵심 로직
├── chat/                           # 실시간 채팅 도메인
│   ├── config/                     # WebSocket/STOMP 프로토콜 설정
│   ├── controller/                 # 메시지 핸들링 및 채팅방 관리 API
│   ├── domain/                     # ChatMessage, ChatRoom 엔티티
│   └── service/                    # 메시지 전송 및 이력 관리
├── config/                         # OpenAPI, CORS, JpaAuditing 설정
├── match/                          # 매칭 알고리즘 도메인
│   ├── controller/                 # 설문, 선호도, 추천 API
│   ├── entity/                     # SurveyResult, MatchScores, MatchStatus
│   └── service/                    # MatchScorer(점수 산출), Recommendation(추천)
├── security/                       # JWT 필터 및 Spring Security 설정
└── exception/                      # 전역 예외 처리(GlobalExceptionHandler)
```

---

## ✨ 주요 기능

### 🎯 지능형 매칭 시스템 (Intelligent Matching)
* **성향 설문 수집**: 사용자 맞춤형 질문 세트를 통해 내면의 가치관 데이터를 수집합니다.
* **매칭 점수 알고리즘**: 설문 결과와 상호 선호도를 분석하여 사용자 간 적합도 점수를 산출합니다.
* **맞춤 추천**: 높은 합치도를 가진 상대를 우선적으로 노출하는 추천 목록을 제공합니다.

### 💬 실시간 소통 서비스 (Real-time Interaction)
* **STOMP 메시징**: WebSocket 기반의 양방향 통신으로 끊김 없는 대화 환경을 구현했습니다.
* **채팅방 관리**: 1:1 매칭 완료 시 전용 채팅방을 자동 생성하며 대화 내역을 보존합니다.

### 📝 인터랙티브 커뮤니티 (Community)
* **피드 시스템**: 사용자들이 일상을 공유하고 소통할 수 있는 게시판 기능을 지원합니다.
* **참여 로직**: 게시글에 대한 댓글 작성 및 시스템 전반의 주요 활동 로그를 기록합니다.

### 🔐 보안 및 인증 (Security)
* **JWT 아키텍처**: Access/Refresh 토큰 구조를 통해 보안성이 강화된 인증 시스템을 구축했습니다.
* **권한 관리**: Spring Security를 활용하여 비인가 사용자의 민감 데이터 접근을 차단합니다.

### 🔄 스왑(SWAP) 시스템 (Roommate Swap)
기존 룸메이트와 맞지 않아 변경을 원하는 사용자를 위한 기능입니다.

**동작 방식:**
1. **스왑 신청**: 사용자가 게시판(FIND 카테고리)에 `status: SWAP_REQUEST`로 게시글 작성
2. **관리자 검토**: 관리자가 신청 내용을 확인하고 승인/거절 결정
3. **상태 변경**:
   - **승인**: `SWAP_APPROVED` → 다른 사용자와 새로운 매칭 가능
   - **거절**: `SWAP_REJECTED` → 기존 룸메이트 유지
4. **재매칭**: 승인된 사용자는 설문 결과를 기반으로 새로운 룸메이트를 매칭받을 수 있음

**상태값:**
- `NORMAL`: 일반 게시글
- `SWAP_REQUEST`: 스왑 신청 대기 중
- `SWAP_APPROVED`: 관리자 승인 완료
- `SWAP_REJECTED`: 관리자 거절

**API 엔드포인트:**
- `POST /api/posts/{id}/approve`: 관리자 승인
- `POST /api/posts/{id}/reject`: 관리자 거절

---

## 🔗 API 엔드포인트

### 인증 API (`/api/auth`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/login` | 로그인 및 JWT 토큰 발행 |
| POST | `/register` | 신규 사용자 가입 |
| POST | `/refresh` | Access Token 갱신 |

### 매칭 API (`/api/matches`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/survey` | 성향 설문 데이터 제출 |
| GET | `/recommendations` | 개인화된 추천 상대 목록 조회 |
| POST | `/status` | 매칭 수락/거절 등 상태 변경 |

### 게시판 API (`/api/board/posts`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/` | 최신 게시글 목록 조회 |
| POST | `/create` | 신규 게시글 작성 |
| GET | `/{id}` | 게시글 상세 내용 및 댓글 조회 |

---

## ⚙️ 설치 및 실행

### 사전 요구사항
* **Java 17** 이상
* **Maven 3.x**
* **MySQL 8.0** 이상

### 로컬 실행
1. **저장소 클론**
   ```bash
   git clone [https://github.com/Every-Moment/every_moment_back.git](https://github.com/Every-Moment/every_moment_back.git)
   cd every_moment_back

2. **빌드**
    ```bash
    ./mvnw clean package
    ```
3. **실행**
    ```
    java -jar target/every_moment_back-0.0.1-SNAPSHOT.jar
    ```

## 💾 데이터베이스 설정
src/main/resources/application.properties 파일에서 아래 정보를 프로젝트 환경에 맞춰 수정합니다.
```
spring.datasource.url=jdbc:mysql://localhost:3306/every_moment_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

---

### DB 생성 명령어
* CREATE DATABASE IF NOT EXISTS dormdb
  CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

* CREATE USER IF NOT EXISTS 'dorm'@'localhost' IDENTIFIED BY 'dormpw';
* GRANT ALL PRIVILEGES ON dormdb.* TO 'dorm'@'localhost';
* FLUSH PRIVILEGES;


파일 구조 정리 및 데이터베이스 연결, 회원가입까지 확인하였습니다.   
(25.09.07 02:49   회원가입시 username 중복되면 오류 발생하여 보완 예정)    
(25.09.08 보완 완료)

* gender추가 관련
* V1이 이미 실행되면 오류가 남 
  * 해결방법 1. # --- Flyway ---
    spring.flyway.enabled=true 여기서 FALSE로 수정
  * 해결방법 2. DB삭제했다 다시 생성


POST 매핑     
회원가입    
/api/school/auth/register
```
{
  "username": "test1",
  "gender":"1",
  "email": "tester@example.com",
  "password": "P@ssw0rd!",
  "smoking": false
}
```
POST  매핑     
로그인     
/api/school/auth/login  

```
    {
      "email": "tester5@example.com",
      "password": "P@ssw0rd!"
    }
```

GET 매핑  
유저정보    
/api/school/user

    Bearer Token: 예시)eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0
    출력
    {
    "data": {
        "id": 4,
        "username": "test1222",
        "gender": 1,
        "email": "tester5@example.com",
        "smoking": false,
        "role": "ROLE_USER",
        "active": true,
        "createdAt": "2025-09-08T14:09:37"
        },
    "timestamp": "2025-09-08T14:15:45.454873600+09:00"
    }
    
