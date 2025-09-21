package com.cmms11.file;

import java.util.List;

public record FileGroupResponse(
    String fileGroupId,
    String refEntity,
    String refId,
    List<FileItemResponse> items
) {
}
