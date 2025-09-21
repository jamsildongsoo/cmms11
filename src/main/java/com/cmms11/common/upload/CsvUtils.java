package com.cmms11.common.upload;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

/** Utility helpers for CSV 기반 대량 업로드 파싱. */
public final class CsvUtils {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final CSVFormat BASE_FORMAT = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .setIgnoreEmptyLines(true)
        .setTrim(true)
        .setAllowMissingColumnNames(false)
        .build();

    private CsvUtils() {
    }

    public static CSVParser parse(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("CSV 파일이 비어 있습니다.");
        }
        Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
        return new CSVParser(reader, BASE_FORMAT);
    }

    public static Map<String, Integer> normalizeHeaderMap(CSVParser parser) {
        Map<String, Integer> headerMap = parser.getHeaderMap();
        if (headerMap == null || headerMap.isEmpty()) {
            throw new IllegalArgumentException("CSV 헤더를 찾을 수 없습니다.");
        }
        Map<String, Integer> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
            String normalizedKey = normalizeHeader(entry.getKey());
            if (normalized.containsKey(normalizedKey)) {
                throw new IllegalArgumentException("중복된 헤더가 존재합니다: " + entry.getKey());
            }
            normalized.put(normalizedKey, entry.getValue());
        }
        return normalized;
    }

    public static void requireHeaders(Map<String, Integer> headerIndex, Collection<String> requiredHeaders) {
        if (requiredHeaders == null || requiredHeaders.isEmpty()) {
            return;
        }
        List<String> missing = requiredHeaders.stream()
            .map(CsvUtils::normalizeHeader)
            .filter(normalized -> !headerIndex.containsKey(normalized))
            .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("필수 헤더가 누락되었습니다: " + String.join(", ", missing));
        }
    }

    public static boolean isEmptyRecord(CSVRecord record) {
        if (record == null) {
            return true;
        }
        for (String value : record) {
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static String getString(CSVRecord record, Map<String, Integer> headerIndex, String column) {
        if (record == null || headerIndex == null) {
            return null;
        }
        Integer index = headerIndex.get(normalizeHeader(column));
        if (index == null) {
            return null;
        }
        String raw = record.get(index);
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String requireNonBlank(String value, String column) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("필수 값이 비어 있습니다: " + column);
        }
        return value;
    }

    public static Integer toInteger(String value, String column) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().replace(",", "");
        try {
            return Integer.valueOf(normalized);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("정수 형식이 올바르지 않습니다(" + column + "): " + value, ex);
        }
    }

    public static BigDecimal toBigDecimal(String value, String column) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().replace(",", "");
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("금액 형식이 올바르지 않습니다(" + column + "): " + value, ex);
        }
    }

    public static LocalDate toLocalDate(String value, String column) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), ISO_DATE);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다(" + column + "): " + value, ex);
        }
    }

    public static int displayRowNumber(CSVRecord record) {
        Objects.requireNonNull(record, "record");
        long recordNumber = record.getRecordNumber();
        long withHeader = recordNumber + 1L;
        if (withHeader > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) withHeader;
    }

    public static String normalizeHeader(String header) {
        if (header == null) {
            return "";
        }
        return header.replace("\ufeff", "").trim().toLowerCase(Locale.ROOT);
    }

    public static Set<String> normalizedHeaders(Collection<String> headers) {
        return headers.stream().map(CsvUtils::normalizeHeader).collect(Collectors.toSet());
    }
}
