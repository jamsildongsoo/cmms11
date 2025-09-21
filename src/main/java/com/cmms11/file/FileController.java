package com.cmms11.file;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileGroupResponse> upload(
        @RequestParam(value = "groupId", required = false) String groupId,
        @RequestParam(value = "refEntity", required = false) String refEntity,
        @RequestParam(value = "refId", required = false) String refId,
        @RequestParam("files") List<MultipartFile> files
    ) {
        FileGroupResponse response = fileService.upload(groupId, refEntity, refId, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<FileGroupResponse> getGroup(@RequestParam("groupId") String groupId) {
        return ResponseEntity.ok(fileService.getGroup(groupId));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> download(
        @PathVariable String fileId,
        @RequestParam("groupId") String groupId
    ) {
        FileDownload download = fileService.download(groupId, fileId);
        String contentType = download.mimeType();
        if (!StringUtils.hasText(contentType)) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        String encodedName = UriUtils.encode(download.originalName(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + download.originalName() + "\"; filename*=UTF-8''" + encodedName
            )
            .contentLength(download.size())
            .body(download.resource());
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(@PathVariable String fileId, @RequestParam("groupId") String groupId) {
        fileService.delete(groupId, fileId);
        return ResponseEntity.noContent().build();
    }
}
