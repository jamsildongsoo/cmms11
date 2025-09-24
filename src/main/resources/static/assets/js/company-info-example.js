/**
 * 회사코드 추출 예시
 * 사용자 정보에서 회사코드를 안전하게 추출하는 방법
 */

// 현재 사용자 정보에서 회사코드 추출하는 방법
function getCurrentUserCompanyId() {
    // 실제로는 백엔드에서 인증된 사용자 정보를 통해 추출
    // 프론트엔드에서는 회사코드를 직접 조작할 수 없음
    
    // 예시: 사용자명이 "C0001:admin" 형태인 경우
    const username = "C0001:admin"; // 실제로는 인증 정보에서 가져옴
    
    if (username && username.includes(":")) {
        const [companyId, memberId] = username.split(":", 2);
        return companyId; // "C0001"
    }
    
    return "C0001"; // 기본값
}

// 파일 업로드 시 회사코드는 백엔드에서 자동으로 결정됨
async function uploadFiles(files) {
    // ❌ 잘못된 방법: 프론트엔드에서 회사코드를 전달
    // const companyId = "C0002"; // 악의적 사용자가 조작 가능
    
    // ✅ 올바른 방법: 백엔드에서 사용자 정보로부터 추출
    const response = await fetch('/api/files/init', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            // companyId를 전달하지 않음 - 백엔드에서 자동 결정
            groupId: this.options.groupId,
            refEntity: this.options.refEntity,
            refId: this.options.refId,
            fileNames: files.map(f => f.name)
        })
    });
    
    // 백엔드에서 인증된 사용자의 회사코드를 사용하여 S3 경로 생성
    // 예: C0001/plant/F250119001/파일명.jpg
}

// 보안 강화 결과
console.log(`
보안 강화 전:
- 프론트엔드에서 회사코드 전달: companyId: "C0002" (조작 가능)
- S3 경로: C0002/plant/F250119001/파일명.jpg (다른 회사 데이터 접근)

보안 강화 후:
- 백엔드에서 사용자 정보로 회사코드 추출: "C0001:admin" -> "C0001"
- S3 경로: C0001/plant/F250119001/파일명.jpg (본인 회사 데이터만 접근)
`);
