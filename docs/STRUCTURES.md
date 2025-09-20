# STRUCTURES

본 문서는 프로젝트의 구조와 규칙을 한눈에 보기 위해 정리되었습니다.
요약: Spring Boot 기반의 얇은 Web 계층, Service 중심의 도메인 접근, JPA 저장소, 정적 템플릿(UI) 구조를 사용합니다.

## 1) 기술스택
- Backend: Java 21, Spring Boot 3.3.x
  - Spring Web, Spring Security(Form Login), Spring Data JPA, Bean Validation
- DB: MariaDB (JDBC 드라이버), Flyway(의존성 포함, 현재 `application.yml`에서 disabled,Hibernate:update)
- Build & Tooling: Gradle, Lombok
- Frontend: 정적 HTML/CSS/JS (resources/templates, resources/static)
  - `defaultLayout.html`을 통한 SPA‑like 내비게이션(History API + fetch)

## 2) 디렉토리 구조

docs/
  CMMS_PRD.md        <- 제품 사양서 
  STRUCTURES.md      <- 본 문서
  TABLES.md          <- 테이블 설계 가이드

src/main/java/com/cmms11/
  Cmms11Application.java
  config/
    SecurityConfig.java   <- 로그인, 접근제어, 폼 로그인 경로 등
    WebConfig.java        <- 정적 리소스/템플릿 매핑
  security/
    MemberUserDetailsService.java <- 사용자 조회/권한 매핑(회사:사용자ID 지원)
  init/
    DataInitializer.java  <- 기본 사용자(seed) 생성
  web/
    HealthController.java
    PlantController.java  <- 예시: 설비 API (현재 저장소 직접 사용)
  common/seq/
    AutoNumberService.java
    Sequence.java / SequenceId.java / SequenceRepository.java / SequenceService.java
  domain/member/
    Member.java / MemberId.java / MemberRepository.java / MemberService.java
  plant/
    Plant.java / PlantId.java / PlantRepository.java / PlantService.java (예시)

src/main/resources/
  application.yml
  db/migration/
    V1__baseline.sql      
  templates/
    auth/login.html
    layout/defaultLayout.html
    plant/list.html, detail.html, form.html
    {타 모듈도 같은 구조이나, domain,code 2가지 모듈은 form.html과 list.html만 필요함 }
  static/assets/
    css/base.css, print.css
    js/app.js             <- 공통 UX 스크립트(첨부 UI 포함)

## 3) Layer와 Naming rule
- Layering
  - Controller: HTTP 매핑, 요청/응답, 간단 검증 및 DTO 변환 담당
  - Service: 트랜잭션 경계, 도메인 규칙/흐름, 저장소 조합 로직 담당
  - Repository(JPA): 엔티티 영속화/조회 쿼리 담당
  - 금지 규칙: Controller는 Repository를 직접 주입/호출하지 않는다. 반드시 Service를 호출한다.

- Package 위치 규칙(기능 공존, feature‑colocation)
  - 엔티티/ID/레포지토리/서비스는 기능별 패키지에 공존: 예) `com.cmms11.plant`
  - 공용 모듈은 `com.cmms11.common.<module>`: 예) `common.seq`
  - 웹 컨트롤러는 `com.cmms11.web` 하위에 위치
  - 인증/인가/계정 등 보안 관련은 `com.cmms11.security` 폴더에 둔다.

- Naming 규칙(Java)
  - Entity: PascalCase (예: `Plant`, `Member`)
  - Embedded ID: `<Entity>NameId` (예: `PlantId`, `MemberId`)
  - Repository: `<Entity>NameRepository` (예: `PlantRepository`)
  - Service: `<Feature>NameService` (예: `PlantService`)
    - 구현체가 여러 개인 경우에만 인터페이스/`Impl` 도입
  - Controller: `<Feature>NameController` (예: `PlantController`)
    mapping에서 {id} 대신 {모듈명+Id}로 명확히 표현 

- Service 계층 메서드 컨벤션(권장)
  - Query: `get*`(없으면 예외), `find*`(Optional-get을 쓸 수 없는 불가피한 경우에만 사용함), `list*/search*`(페이지 결과)
  - Command: `create*`, `update*`, `delete*`(검증/중복제어를 Service에서 처리)
  - `@Transactional`은 Service에 적용(클래스 또는 메서드 레벨)

- Controller/Repository 계층 메서드 컨벤션(권장)
  - Controller: `list*`, `get*`, `create*`, `update*`, `delete*` 등 HTTP 동사/행위를 반영하고 Service로 위임
    - DTO/바인딩 객체는 `*Request`, `*Response` 명명 권장
  - Repository: Spring Data 규칙에 맞춘 파생 쿼리(`findBy*`, `existsBy*`, `deleteBy*`) 또는 `@Query` 사용
    - 페이징은 `Page<T> method(..., Pageable pageable)` 시그니처 유지

## 4) 기타

### 로그인 절차
- 로그인 페이지: `/auth/login.html`
- 처리 URL: `/api/auth/login` (Spring Security formLogin)
- 파라미터: `member_id`(사용자ID), `password`
- 사용자 식별 규칙:
  - 기본 회사코드: `C0001`
  - `member_id`가 `회사코드:사용자ID` 형태라면 분리 처리(예: `C0002:admin`)
- 성공 시 이동: `/layout/defaultLayout.html?content=/plant/list.html`
- 실패 시 이동: `/auth/login.html?error=1`
- 로그아웃: `/api/auth/logout` → `/auth/login.html`

### defaultLayout 구조(내비게이션)
- 파일: `src/main/resources/templates/layout/defaultLayout.html`
- 동작: 쿼리스트링 `content` 경로를 fetch로 로딩하여 내부 `#layout-slot`에 삽입
  - 링크 클릭을 가로채 History API로 경로만 변경하여 페이지 전환 느낌 제공
  - 초기 진입 시 또는 뒤/앞으로 가기(popstate) 처리 포함
- 신규 화면 추가 시
  - `templates/<feature>/<view>.html`에 화면 정의
  - 레이아웃에서 `?content=/<feature>/<view>.html`로 연결

### 자동번호 채번 규칙
- 구현: `common.seq.AutoNumberService`
- 저장소/락: `Sequence` 테이블을 비관적 쓰기 락으로 조회(`findForUpdate`) 후 증분
- Master 번호: `{moduleCode(1)}{연동키 '000000'} + 9자리 시퀀스`
  - API: `generateMasterId(companyId, moduleCode)`
  - 권장 모듈코드 매핑(master): `plant=1`, `inventory=2`
- Transaction 번호: `{moduleCode(1)}{YYMMDD}{3자리 시퀀스}`
  - API: `generateTxId(companyId, moduleCode, date)`
  - 권장 모듈코드 매핑(transaction): `inspection=I`, `workorder=O`, `workpermit=P`, `approvals=A`, `file_group_id=F`
- 초기값/증분: `next_seq`에서 현재값 반환 후 +1 저장
- 신규 생성: `sequence` 테이블에 (companyId, moduleCode, dateKey) 행이 없으면 신규로 생성 후 시퀀스 시작(코드에 반영됨)

### 파일 업로드(현황 및 계획)
- 현황
  - UI: 설비 폼(`plant/form.html`)에 다중 파일 입력 및 목록 표시(UI 전용) 존재
  - 도메인: `Plant.fileGroupId` 칼럼만 존재(첨부 그룹 식별자)
  - 백엔드 업로드/다운로드 API는 아직 미구현
- 권장 설계(차기)
  - API: `POST /api/files`(multipart/form-data, 반환: `fileGroupId`와 항목 목록)
    - 항목 단건 삭제: `DELETE /api/files/{fileId}`
    - 그룹 조회: `GET /api/files?groupId=...`
    - 다운로드: `GET /api/files/{fileId}` (권한/범위 검증 포함)
  - 보관: 외부 스토리지(디스크/S3 등) + 메타 테이블(`file_group`, `file_item` 등)
  - 연계: 생성/수정 시 `Plant.fileGroupId`에 그룹ID 저장 → 상세/목록에서 파일 수/미리보기 제공
  - 제약: 크기 제한, 확장자 화이트리스트, 바이러스 스캔(옵션), 다운로드 권한 체크

## 5) 디자인 가이드(plant 모듈 기준 공통 규칙)

- 페이지 골격(standalone)
  - 상단 앱바: <header class="appbar"><div class="appbar-inner">...</div></header>
  - 브레드크럼: <nav class="breadcrumbs"> ... </nav> (구분 기호는 <span class="sep">/</span>)
  - 본문 컨테이너: <main><div class="container"> ... </div></main>
  - 푸터: <footer><div class="container">© ...</div></footer>
  - 예시 파일: plant/list.html, plant/form.html, plant/detail.html

- 레이아웃 사용 규칙(defaultLayout)
  - 파일: templates/layout/defaultLayout.html
  - 동작: ?content=/경로/화면.html을 읽어 <main> 내부만 #layout-slot에 삽입
  - 주의: content 페이지의 <head>는 무시됨. 공용 CSS/JS는 레이아웃에서 로드됨
    - 페이지 전용 스타일이 필요하면 공용 CSS 확장 또는 본문 내 인라인 스타일 사용
  - 링크는 일반 <a href="..."> 사용(기본 내비게이션). 레이아웃이 History API로 인터셉트

- 카드/섹션 구성
  - 카드: <section class="card"> 안에 card-header(좌: card-title, 우: toolbar), card-body
  - 섹션: card-body 안을 여러 div.section으로 분할하고 제목은 div.section-title
  - 예시: plant/list.html의 카드/툴바, plant/form.html의 섹션 구조

- 그리드/레이아웃 유틸
  - 12열 그리드: 부모 div.grid.cols-12 + 자식 div.col-span-{1..12}
  - 읽기 전용 스택: div.stack(라벨/값 세로쌓기), 폼 행은 div.form-row
  - 가변 너비/정렬: .row, .spacer(좌우 여백 채우기)

- 테이블 패턴
  - 클래스: <table class="table"> + 표준 <thead>/<tbody>
  - 행 클릭 이동: <tr data-row-link="detail.html"> 패턴 사용(static/assets/js/app.js 처리)
  - 첫 컬럼에는 <a href="..."> 유지(접근성/우클릭 열기 지원)

- 버튼/배지/알림
  - 버튼: .btn, 강조 .btn.primary, 소형 .btn.sm, 위험 .btn.danger
  - 배지: .badge
  - 안내/에러: .notice, 에러 문구 .danger-text

- 폼 가이드
  - <form data-validate>로 브라우저 기본 검증 활성화(app.js가 checkValidity 연동)
  - 라벨: <label class="label [required]" for="...">, 입력: .input, select, textarea
  - 필수/길이/형식: required, maxlength, type="email|number|date" 등 HTML 속성 사용
  - 확인 경고: data-confirm 속성으로 confirm 인터셉트

- 첨부 파일 UI(프론트 전용)
  - 래퍼: <div class="section" data-attachments>
  - 숨김 인풋: <input id="attachments-input" type="file" multiple class="visually-hidden" />
  - 추가 버튼: <button type="button" class="btn" data-attachments-add>
  - 목록: <ul class="attachments-list" aria-live="polite"> + 항목 .attachment-item
  - 삭제 버튼: .btn-remove(+ aria-label 적용)
  - 접근성: 파일 목록은 aria-live="polite" 유지, 빈 상태는 <li class="empty">

- 인쇄 양식
  - 인쇄 전용 CSS: static/assets/css/print.css
  - DOM: <section class="print-form"><div class="doc"> ... </div></section> 구조
  - 트리거: “출력 미리보기”/“인쇄” 버튼(plant/detail.html 참고)

- 정적 자산/경로
  - 레이아웃 로드: /assets/css/base.css, /assets/js/app.js
  - 단독 페이지 미리보기 시: 상대 경로 ../../static/assets/... 사용 가능
  - 리소스 매핑: /assets/** → classpath:/static/assets/ (WebConfig 참고)

- SPA 내비게이션 규칙
  - 일반 링크 클릭을 레이아웃이 인터셉트해 content로 로드, 뒤/앞으로 가기(popstate) 지원
  - 외부 링크/http, mailto, target="_blank", # 앵커는 인터셉트 제외

- 접근성/사용성 체크리스트
  - 포커스 가능한 첫 번째 오류 필드로 포커스 이동(form[data-validate])
  - 삭제/위험동작은 data-confirm으로 확인 절차 제공
  - 테이블 행 클릭 이동과 앵커 병행 제공으로 키보드/마우스 모두 지원

## 6) 메뉴 Tree 

CMMS 메인 메뉴


### 메뉴별 화면 구성
- **list.html**: 목록 조회 (검색, 페이징, 신규등록 버튼)
- **form.html**: 등록/수정 폼 (신규 등록 시 ID 자동 생성)  
- **detail.html**: 상세 조회 (읽기 전용, 수정/삭제 버튼)

### 도메인별 특징
- **domain 모듈**: 기준정보로 form.html, list.html만 제공 (상세조회 불필요)
- **transaction 모듈**: 업무 데이터로 list.html, form.html, detail.html 모두 제공
- **master 모듈**: 자산정보로 모든 화면 제공

### 자동번호 채번 규칙 (메뉴별)
- **Master ID**: `{moduleCode(1)}{000000}{9자리시퀀스}`
  - Plant(설비): moduleCode=1 → P100000001
  - Inventory(재고): moduleCode=2 → I200000001
- **Transaction ID**: `{moduleCode(1)}{YYMMDD}{3자리시퀀스}`
  - Inspection(점검): moduleCode=I → I250119001  
  - WorkOrder(작업지시): moduleCode=O → O250119001
  - WorkPermit(작업허가): moduleCode=P → P250119001
  - Approval(승인): moduleCode=A → A250119001 