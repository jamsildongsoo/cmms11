# CMMS11 모바일 전략 (Flutter)
작성일: 2025-09-25 00:33:53

## 1. 범위
- 신규 작성 없음: 목록 조회 + 결과 입력/결재 승인만 수행
- 대상 모듈: memo, approval(결재), inspection, workorder, workpermit, plant(조회), inventory(조회)

## 2. API 전략
- 원칙: 서버 공용 API 재사용. 단, 뷰/세션의존 제거 필요
- 권장 경로: `/api/mobile/v1/**` (세션/쿠키 대신 토큰 기반)
- 공통 응답 DTO: `Paged<T>`, `SimpleResult`

## 3. 인증/인가
- OAuth2 Password(사내) 또는 FormLogin + Token 교환
- Multi-company: 서브도메인 또는 로그인시 companyId 입력

## 4. 화면 설계
- 홈: 큰 카드형 진입(모듈별)
- 각 모듈: 목록 → 상세/입력(간소 UI)
- Inspection/WorkOrder: 결과값 입력 → 저장/확정
- Approval: 라인/코멘트/승인/반려

## 5. 동기/오프라인
- 오프라인 임시저장(선택). 연결 시 동기화

## 6. 보안
- HTTPS 강제, 토큰 만료/갱신, 기기 분실 시 토큰 폐기

## 7. 성능/품질
- 첫 화면 1.5s 이내, 목록 페이징, 이미지/Lazy
- Crashlytics/로깅, e2e 테스트

## 8. 할 일 체크리스트
- [ ] `/api/mobile/v1` 스켈레톤 생성
- [ ] DTO 축소(모바일 전용)
- [ ] 토큰 발급/갱신 API
- [ ] 결재/점검/작업 입력 폼 샘플
- [ ] 배포(Play/Internal)