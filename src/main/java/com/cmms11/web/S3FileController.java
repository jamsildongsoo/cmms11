package com.cmms11.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cmms11.security.MemberUserDetailsService;
import com.cmms11.file.S3FileService;
import com.cmms11.file.FileGroupResponse;

import java.util.List;

@RestController
@RequestMapping("/api/s3-files")
public class S3FileController {

    private final S3FileService fileService;

    public S3FileController(S3FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 업로드 세션 생성 - Presigned URL 반환
     */
    @PostMapping("/init")
    public ResponseEntity<S3FileService.FileUploadSession> initUpload(@RequestBody UploadInitRequest request) {
        // 독립적인 모듈: companyId를 파라미터로 전달
        String companyId = MemberUserDetailsService.getCurrentUserCompanyId();
        
        S3FileService.FileUploadSession session = fileService.createUploadSession(
            companyId,
            request.groupId(),
            request.refEntity(),
            request.refId(),
            request.fileNames()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    /**
     * 업로드 완료 - 메타데이터 저장
     */
    @PostMapping("/{groupId}/complete")
    public ResponseEntity<FileGroupResponse> completeUpload(
        @PathVariable String groupId,
        @RequestBody List<S3FileService.FileMetadata> fileMetadatas
    ) {
        String companyId = MemberUserDetailsService.getCurrentUserCompanyId();
        FileGroupResponse response = fileService.completeUpload(companyId, groupId, fileMetadatas);
        return ResponseEntity.ok(response);
    }

    /**
     * 파일 그룹 조회
     */
    @GetMapping
    public ResponseEntity<FileGroupResponse> getGroup(@RequestParam("groupId") String groupId) {
        String companyId = MemberUserDetailsService.getCurrentUserCompanyId();
        return ResponseEntity.ok(fileService.getGroup(companyId, groupId));
    }

    /**
     * 파일 다운로드 URL 생성
     */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<DownloadUrlResponse> getDownloadUrl(
        @PathVariable String fileId,
        @RequestParam("groupId") String groupId
    ) {
        String companyId = MemberUserDetailsService.getCurrentUserCompanyId();
        String downloadUrl = fileService.getDownloadUrl(companyId, groupId, fileId);
        return ResponseEntity.ok(new DownloadUrlResponse(downloadUrl));
    }

    /**
     * 파일 삭제
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(@PathVariable String fileId, @RequestParam("groupId") String groupId) {
        String companyId = MemberUserDetailsService.getCurrentUserCompanyId();
        fileService.delete(companyId, groupId, fileId);
        return ResponseEntity.noContent().build();
    }

    // Request/Response DTOs
    public record UploadInitRequest(
        String groupId,
        String refEntity,
        String refId,
        List<String> fileNames
    ) {}

    public record DownloadUrlResponse(String downloadUrl) {}
}
