package com.cmms11.file;

import org.springframework.core.io.Resource;

public record FileDownload(Resource resource, String originalName, String mimeType, long size) {
}
