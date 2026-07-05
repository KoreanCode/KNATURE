package com.knature.bo.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 엑셀(.xlsx) 다운로드 응답 생성.
 * - String → 텍스트 셀 (송장번호 등 긴 숫자 식별자의 지수표기(1.23E+11) 방지)
 * - Number → 숫자 셀 (#,##0 포맷)
 * - LocalDateTime → 날짜 셀 (yyyy-mm-dd hh:mm 포맷)
 * - 컬럼 폭은 콘텐츠에 맞춰 자동 조정 (한글 보정 패딩 포함)
 */
public final class ExcelUtil {

    private ExcelUtil() {}

    public static ResponseEntity<byte[]> download(String filename, String sheetName,
                                                  String[] headers, List<Object[]> rows) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet(sheetName);

            // 스타일
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            DataFormat df = wb.createDataFormat();
            CellStyle numberStyle = wb.createCellStyle();
            numberStyle.setDataFormat(df.getFormat("#,##0"));
            CellStyle textStyle = wb.createCellStyle();
            textStyle.setDataFormat(df.getFormat("@")); // 텍스트 서식
            CellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(df.getFormat("yyyy-mm-dd hh:mm"));

            // 헤더
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 데이터 — 타입별 셀
            for (int r = 0; r < rows.size(); r++) {
                Row row = sheet.createRow(r + 1);
                Object[] values = rows.get(r);
                for (int c = 0; c < values.length; c++) {
                    Cell cell = row.createCell(c);
                    Object v = values[c];
                    if (v == null) {
                        cell.setCellValue("");
                        cell.setCellStyle(textStyle);
                    } else if (v instanceof Number n) {
                        cell.setCellValue(n.doubleValue());
                        cell.setCellStyle(numberStyle);
                    } else if (v instanceof LocalDateTime dt) {
                        cell.setCellValue(dt);
                        cell.setCellStyle(dateStyle);
                    } else {
                        cell.setCellValue(v.toString());
                        cell.setCellStyle(textStyle);
                    }
                }
            }

            // 컬럼 폭: 콘텐츠 기준 자동 조정 + 한글 폭 보정
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int width = (int) Math.min(sheet.getColumnWidth(i) * 1.3 + 512, 255 * 256);
                sheet.setColumnWidth(i, width);
            }

            wb.write(out);
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(out.toByteArray());
        }
    }
}
