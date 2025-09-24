package com.cmms11.file;

import com.cmms11.common.error.NotFoundException;
import com.cmms11.common.seq.AutoNumberService;
import com.cmms11.security.MemberUserDetailsService;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class S3FileService {

    private static final String MODULE_CODE = "F";
    private static final String DELETE_MARK_Y = "Y";
    private static final Duration PRESIGNED_URL_EXPIRY = Duration.ofMinutes(15);

    private final FileGroupRepository groupRepository;
    private final FileItemRepository itemRepository;
    private final AutoNumberService autoNumberService;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final boolean preserveOriginalFilename;

    public S3FileService(
        FileGroupRepository groupRepository,
        FileItemRepository itemRepository,
        AutoNumberService autoNumberService,
        @Value("${aws.s3.bucket-name}") String bucketName,
        @Value("${aws.s3.preserve-original-filename:true}") boolean preserveOriginalFilename
    ) {
        this.groupRepository = groupRepository;
        this.itemRepository = itemRepository;
        this.autoNumberService = autoNumberService;
        this.bucketName = bucketName;
        this.preserveOriginalFilename = preserveOriginalFilename;
        
        // S3 클라이언트 초기화
        this.s3Client = S3Client.builder()
            .region(Region.AP_NORTHEAST_2) // 서울 리전
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
            
        this.s3Presigner = S3Presigner.builder()
            .region(Region.AP_NORTHEAST_2)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    /**
     * 업로드 세션 생성 - Presigned URL 반환
     */
    public FileUploadSession createUploadSession(String companyId, String groupId, String refEntity, String refId, List<String> fileNames) {
        LocalDateTime now = LocalDateTime.now();
        String memberId = getCurrentMemberId();

        // FileGroup 생성/조회
        FileGroup group = resolveGroup(groupId, refEntity, refId, companyId, memberId, now);
        group.setUpdatedAt(now);
        group.setUpdatedBy(memberId);
        group = groupRepository.save(group);

        // 각 파일에 대한 Presigned URL 생성
        List<FileUploadInfo> uploadInfos = new ArrayList<>();
        for (String fileName : fileNames) {
            String fileId = autoNumberService.generateTxId(companyId, MODULE_CODE, LocalDate.now());
            String s3Key = generateS3Key(companyId, refEntity, group.getId().getFileGroupId(), fileName);
            
            // Presigned URL 생성
            String presignedUrl = generatePresignedUrl(s3Key, fileName);
            
            uploadInfos.add(new FileUploadInfo(fileId, fileName, s3Key, presignedUrl));
        }

        return new FileUploadSession(group.getId().getFileGroupId(), uploadInfos);
    }

    /**
     * 업로드 완료 후 메타데이터 저장
     */
    public FileGroupResponse completeUpload(String companyId, String groupId, List<FileMetadata> fileMetadatas) {
        LocalDateTime now = LocalDateTime.now();
        String memberId = getCurrentMemberId();

        FileGroup group = groupRepository
            .findByIdCompanyIdAndIdFileGroupId(companyId, groupId)
            .orElseThrow(() -> new NotFoundException("파일 그룹을 찾을 수 없습니다: " + groupId));

        // 최대 라인 번호 조회
        Integer maxLineNo = itemRepository.findMaxLineNo(companyId, groupId);
        int currentLineNo = maxLineNo != null ? maxLineNo : 0;

        // 각 파일의 메타데이터 저장
        for (FileMetadata metadata : fileMetadatas) {
            FileItem item = new FileItem();
            item.setId(new FileItemId(companyId, groupId, metadata.fileId()));
            item.setLineNo(++currentLineNo);
            item.setOriginalName(metadata.originalName());
            item.setStoredName(metadata.originalName()); // 원본 파일명 유지
            item.setExt(extractExtension(metadata.originalName()));
            item.setMime(metadata.contentType());
            item.setSize(metadata.size());
            item.setChecksumSha256(metadata.checksum());
            item.setStoragePath(metadata.s3Key()); // S3 키를 storage_path에 저장
            item.setDeleteMark("N");
            item.setCreatedAt(now);
            item.setCreatedBy(memberId);
            item.setUpdatedAt(now);
            item.setUpdatedBy(memberId);
            
            itemRepository.save(item);
        }

        // 그룹 업데이트
        group.setUpdatedAt(now);
        group.setUpdatedBy(memberId);
        groupRepository.save(group);

        return toResponse(group, activeItems(companyId, groupId));
    }

    /**
     * 파일 그룹 조회
     */
    @Transactional(readOnly = true)
    public FileGroupResponse getGroup(String companyId, String groupId) {
        if (!StringUtils.hasText(groupId)) {
            throw new IllegalArgumentException("fileGroupId는 필수입니다.");
        }
        FileGroup group = groupRepository
            .findByIdCompanyIdAndIdFileGroupId(companyId, groupId)
            .orElseThrow(() -> new NotFoundException("파일 그룹을 찾을 수 없습니다: " + groupId));
        return toResponse(group, activeItems(companyId, groupId));
    }

    /**
     * 파일 다운로드 URL 생성 (Presigned URL)
     */
    @Transactional(readOnly = true)
    public String getDownloadUrl(String companyId, String groupId, String fileId) {
        FileItem item = requireActiveFile(companyId, groupId, fileId);
        return generateDownloadPresignedUrl(item.getStoragePath(), item.getOriginalName());
    }

    /**
     * 파일 삭제
     */
    public void delete(String companyId, String groupId, String fileId) {
        FileItem item = requireActiveFile(companyId, groupId, fileId);
        
        // S3에서 파일 삭제
        try {
            s3Client.deleteObject(builder -> builder
                .bucket(bucketName)
                .key(item.getStoragePath())
            );
        } catch (Exception e) {
            throw new IllegalStateException("S3에서 파일을 삭제할 수 없습니다: " + fileId, e);
        }

        // DB에서 소프트 삭제
        LocalDateTime now = LocalDateTime.now();
        String memberId = getCurrentMemberId();
        item.setDeleteMark(DELETE_MARK_Y);
        item.setUpdatedAt(now);
        item.setUpdatedBy(memberId);
        itemRepository.save(item);

        // 그룹 업데이트
        FileGroup group = groupRepository
            .findByIdCompanyIdAndIdFileGroupId(getCurrentUserCompanyId(), groupId)
            .orElseThrow(() -> new NotFoundException("파일 그룹을 찾을 수 없습니다: " + groupId));
        group.setUpdatedAt(now);
        group.setUpdatedBy(memberId);
        groupRepository.save(group);
    }

    // Private helper methods

    private FileGroup resolveGroup(String requestedGroupId, String refEntity, String refId, 
                                  String companyId, String memberId, LocalDateTime now) {
        if (StringUtils.hasText(requestedGroupId)) {
            FileGroup existing = groupRepository
                .findByIdCompanyIdAndIdFileGroupId(companyId, requestedGroupId)
                .orElseThrow(() -> new NotFoundException("파일 그룹을 찾을 수 없습니다: " + requestedGroupId));
            if (StringUtils.hasText(refEntity)) {
                existing.setRefEntity(refEntity);
            }
            if (StringUtils.hasText(refId)) {
                existing.setRefId(refId);
            }
            return existing;
        }
        
        FileGroup group = new FileGroup();
        String newGroupId = autoNumberService.generateTxId(companyId, MODULE_CODE, LocalDate.now());
        group.setId(new FileGroupId(companyId, newGroupId));
        group.setDeleteMark("N");
        group.setRefEntity(refEntity);
        group.setRefId(refId);
        group.setCreatedAt(now);
        group.setCreatedBy(memberId);
        return group;
    }

    private String generateS3Key(String companyId, String refEntity, String groupId, String fileName) {
        // 원본 파일명을 URL 인코딩하여 안전하게 처리
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        return String.format("%s/%s/%s/%s", companyId, refEntity, groupId, encodedFileName);
    }

    private String generatePresignedUrl(String s3Key, String fileName) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(s3Key)
            .contentType(detectContentType(fileName))
            .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(PRESIGNED_URL_EXPIRY)
            .putObjectRequest(putObjectRequest)
            .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return presignedRequest.url().toString();
    }

    private String generateDownloadPresignedUrl(String s3Key, String fileName) {
        // GET 요청용 Presigned URL 생성 로직
        // 구현 생략 - 필요시 추가
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, s3Key);
    }

    private String detectContentType(String fileName) {
        String extension = extractExtension(fileName).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "txt" -> "text/plain";
            default -> "application/octet-stream";
        };
    }

    private String extractExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1);
    }

    private List<FileItemResponse> activeItems(String companyId, String groupId) {
        return itemRepository
            .findByIdCompanyIdAndIdFileGroupIdAndDeleteMarkNot(companyId, groupId, DELETE_MARK_Y)
            .stream()
            .sorted(Comparator.comparing(FileItem::getLineNo))
            .map(FileItemResponse::from)
            .toList();
    }

    private FileGroupResponse toResponse(FileGroup group, List<FileItemResponse> items) {
        return new FileGroupResponse(group.getId().getFileGroupId(), group.getRefEntity(), group.getRefId(), items);
    }

    private FileItem requireActiveFile(String companyId, String groupId, String fileId) {
        if (!StringUtils.hasText(groupId)) {
            throw new IllegalArgumentException("fileGroupId는 필수입니다.");
        }
        if (!StringUtils.hasText(fileId)) {
            throw new IllegalArgumentException("fileId는 필수입니다.");
        }
        return itemRepository
            .findByIdCompanyIdAndIdFileGroupIdAndIdFileIdAndDeleteMarkNot(
                companyId,
                groupId,
                fileId,
                DELETE_MARK_Y
            )
            .orElseThrow(() -> new NotFoundException("파일을 찾을 수 없습니다: " + fileId));
    }

    private String getCurrentUserCompanyId() {
        // 독립적인 모듈: 정적 메서드로 회사코드 추출
        return MemberUserDetailsService.getCurrentUserCompanyId();
    }

    private String getCurrentMemberId() {
        // 독립적인 모듈: 정적 메서드로 멤버 ID 추출
        return MemberUserDetailsService.getCurrentMemberId();
    }

    // DTO 클래스들
    public record FileUploadSession(String groupId, List<FileUploadInfo> uploadInfos) {}
    
    public record FileUploadInfo(String fileId, String fileName, String s3Key, String presignedUrl) {}
    
    public record FileMetadata(String fileId, String originalName, String s3Key, String contentType, Long size, String checksum) {}
}
