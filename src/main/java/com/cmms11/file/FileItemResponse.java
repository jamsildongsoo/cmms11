package com.cmms11.file;

public record FileItemResponse(
    String fileId,
    Integer lineNo,
    String originalName,
    Long size,
    String mimeType
) {

    public static FileItemResponse from(FileItem item) {
        return new FileItemResponse(
            item.getId().getFileId(),
            item.getLineNo(),
            item.getOriginalName(),
            item.getSize(),
            item.getMime()
        );
    }
}
