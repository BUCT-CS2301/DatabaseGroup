package com.platform.admin.modules.artifact.support;

import com.platform.admin.common.BusinessException;
import com.platform.admin.common.ErrorCode;
import com.platform.admin.modules.artifact.dto.CreateRelicRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文物 CSV 导入：表头规范化、RFC4180 解析、行校验与重复检测（PRD V1.3.1）。
 */
@Component
public class RelicCsvImportParser {

    /** 与 {@link CreateRelicRequest} 字段声明顺序一致，用于 11 元组重复比较。 */
    public static final List<String> PROPERTY_ORDER = List.of(
            "title",
            "period",
            "type",
            "material",
            "description",
            "dimensions",
            "museumId",
            "detailUrl",
            "creditLine",
            "accessionNumber",
            "crawlDate");

    private static final DateTimeFormatter CRAWL_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int MAX_DATA_ROWS = 2000;
    private static final long MAX_FILE_BYTES = 10L * 1024 * 1024;

    private final Validator validator;

    public RelicCsvImportParser(Validator validator) {
        this.validator = validator;
    }

    /**
     * 解析并校验 CSV，不访问数据库；失败抛出 {@link BusinessException}（400）。
     *
     * @param file multipart 字段 {@code file}
     * @return 按数据行顺序排列的请求列表
     */
    public List<CreateRelicRequest> parseAndValidate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "缺少 CSV 文件或文件为空");
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new BusinessException(ErrorCode.PAYLOAD_TOO_LARGE, "CSV 文件不能超过 10 MB");
        }
        String original = file.getOriginalFilename();
        if (original == null || !original.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请上传 .csv 文件");
        }
        byte[] raw;
        try {
            raw = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "读取 CSV 文件失败");
        }
        int bomOffset = 0;
        if (raw.length >= 3 && (raw[0] & 0xFF) == 0xEF && (raw[1] & 0xFF) == 0xBB && (raw[2] & 0xFF) == 0xBF) {
            bomOffset = 3;
        }
        String text = decodeStrictUtf8(raw, bomOffset, raw.length - bomOffset);

        CSVFormat format = CSVFormat.Builder.create(CSVFormat.RFC4180)
                .setIgnoreEmptyLines(false)
                .build();
        List<CSVRecord> records;
        try (CSVParser parser = CSVParser.parse(new StringReader(text), format)) {
            records = parser.getRecords();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "CSV 解析失败");
        }
        if (records.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "CSV 无表头或内容为空");
        }
        CSVRecord headerRecord = records.get(0);
        if (headerRecord.size() == 0 || headerRowBlank(headerRecord)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "CSV 表头无效或首行为空");
        }
        Map<String, Integer> propertyToColumn = mapHeader(headerRecord);

        List<CSVRecord> dataRecords = new ArrayList<>();
        for (int i = 1; i < records.size(); i++) {
            CSVRecord rec = records.get(i);
            if (isSkippableEmptyRow(rec)) {
                continue;
            }
            dataRecords.add(rec);
        }
        if (dataRecords.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "CSV 无数据行");
        }
        if (dataRecords.size() > MAX_DATA_ROWS) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "CSV 数据行超过上限 " + MAX_DATA_ROWS + " 行");
        }

        List<CreateRelicRequest> requests = new ArrayList<>(dataRecords.size());
        Set<String> tupleSignatures = new HashSet<>();
        int excelRow = 2;
        for (CSVRecord row : dataRecords) {
            validateRequiredCells(row, propertyToColumn, excelRow);
            CreateRelicRequest req = buildRequest(row, propertyToColumn);
            applyTypesAndBeanValidate(req, excelRow);
            String sig = tupleSignature(row, propertyToColumn);
            if (!tupleSignatures.add(sig)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "存在重复的数据行（第 " + excelRow + " 行与前行 11 列取值全同）");
            }
            requests.add(req);
            excelRow++;
        }
        return requests;
    }

    private static boolean headerRowBlank(CSVRecord headerRecord) {
        for (String v : headerRecord) {
            if (v != null && !v.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSkippableEmptyRow(CSVRecord rec) {
        if (rec.size() == 0) {
            return true;
        }
        for (String v : rec) {
            if (v != null && !v.isBlank()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 表头规范化：小写、去 ASCII 下划线、去 Unicode 空白（与 PRD 一致）。
     */
    public static String normalizeHeaderKey(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.codePoints()
                .filter(cp -> cp != '_' && !Character.isWhitespace(cp))
                .collect(
                        StringBuilder::new,
                        (sb, cp) -> sb.appendCodePoint(Character.toLowerCase(cp)),
                        StringBuilder::append)
                .toString();
    }

    private static String decodeStrictUtf8(byte[] raw, int offset, int len) {
        CharsetDecoder dec = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            return dec.decode(ByteBuffer.wrap(raw, offset, len)).toString();
        } catch (CharacterCodingException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "CSV 须为 UTF-8 编码");
        }
    }

    private Map<String, Integer> mapHeader(CSVRecord headerRecord) {
        Map<String, Integer> propertyColumn = new LinkedHashMap<>();
        Map<String, String> normKeyToCanonical = new HashMap<>();
        for (String prop : PROPERTY_ORDER) {
            normKeyToCanonical.put(normalizeJavaPropertyName(prop), prop);
        }
        int maxCol = headerRecord.size();
        for (int c = 0; c < maxCol; c++) {
            String cell = headerRecord.get(c);
            if (cell == null || cell.isBlank()) {
                continue;
            }
            String norm = normalizeHeaderKey(cell);
            if (norm.isEmpty()) {
                continue;
            }
            String property = normKeyToCanonical.get(norm);
            if (property == null) {
                continue;
            }
            if (propertyColumn.containsKey(property)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "表头列冲突：多个列映射到属性 " + property);
            }
            propertyColumn.put(property, c);
        }
        List<String> missing = new ArrayList<>();
        for (String prop : PROPERTY_ORDER) {
            if (!propertyColumn.containsKey(prop)) {
                missing.add(prop);
            }
        }
        if (!missing.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.BAD_REQUEST, "表头缺列，须覆盖 CreateRelicRequest 全部属性，缺少: " + String.join(", ", missing));
        }
        return propertyColumn;
    }

    private static String normalizeJavaPropertyName(String javaName) {
        return normalizeHeaderKey(javaName);
    }

    private static String cellAt(CSVRecord row, int colIdx) {
        if (colIdx < 0 || colIdx >= row.size()) {
            return "";
        }
        String v = row.get(colIdx);
        return v == null ? "" : v;
    }

    private void validateRequiredCells(CSVRecord row, Map<String, Integer> colMap, int excelRow) {
        String title = cellAt(row, colMap.get("title")).trim();
        String detailUrl = cellAt(row, colMap.get("detailUrl")).trim();
        String museumId = cellAt(row, colMap.get("museumId")).trim();
        String crawlRaw = cellAt(row, colMap.get("crawlDate")).trim();
        if (title.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "第 " + excelRow + " 行 title 不能为空");
        }
        if (detailUrl.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "第 " + excelRow + " 行 detailUrl 不能为空");
        }
        if (museumId.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "第 " + excelRow + " 行 museumId 不能为空");
        }
        if (crawlRaw.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "第 " + excelRow + " 行 crawlDate 不能为空");
        }
        try {
            LocalDate.parse(crawlRaw, CRAWL_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "第 " + excelRow + " 行 crawlDate 须为 yyyy-MM-dd");
        }
    }

    private CreateRelicRequest buildRequest(CSVRecord row, Map<String, Integer> colMap) {
        CreateRelicRequest r = new CreateRelicRequest();
        r.setTitle(trimToNullOrText(cellAt(row, colMap.get("title")), false));
        r.setPeriod(trimToNullOrText(cellAt(row, colMap.get("period")), true));
        r.setType(trimToNullOrText(cellAt(row, colMap.get("type")), true));
        r.setMaterial(trimToNullOrText(cellAt(row, colMap.get("material")), true));
        r.setDescription(trimToNullOrText(cellAt(row, colMap.get("description")), true));
        r.setDimensions(trimToNullOrText(cellAt(row, colMap.get("dimensions")), true));
        r.setMuseumId(trimToNullOrText(cellAt(row, colMap.get("museumId")), false));
        r.setDetailUrl(trimToNullOrText(cellAt(row, colMap.get("detailUrl")), false));
        r.setCreditLine(trimToNullOrText(cellAt(row, colMap.get("creditLine")), true));
        r.setAccessionNumber(trimToNullOrText(cellAt(row, colMap.get("accessionNumber")), true));
        String crawlRaw = cellAt(row, colMap.get("crawlDate")).trim();
        r.setCrawlDate(crawlRaw.isEmpty() ? null : LocalDate.parse(crawlRaw, CRAWL_DATE_FORMAT));
        return r;
    }

    private static String trimToNullOrText(String raw, boolean optional) {
        if (raw == null || raw.isBlank()) {
            return optional ? null : "";
        }
        return raw.trim();
    }

    private void applyTypesAndBeanValidate(CreateRelicRequest req, int excelRow) {
        Set<ConstraintViolation<CreateRelicRequest>> violations = validator.validate(req);
        if (!violations.isEmpty()) {
            String msg = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            throw new BusinessException(ErrorCode.BAD_REQUEST, "第 " + excelRow + " 行: " + msg);
        }
    }

    private String tupleSignature(CSVRecord row, Map<String, Integer> colMap) {
        List<String> parts = new ArrayList<>(PROPERTY_ORDER.size());
        for (String prop : PROPERTY_ORDER) {
            parts.add(cellAt(row, colMap.get(prop)).trim());
        }
        return String.join("\u0000", parts);
    }
}
