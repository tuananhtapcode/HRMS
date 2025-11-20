package com.project.hrms.utils.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.util.function.Consumer;

public class ExcelUtils {

    /**
     * Đọc từng dòng trong file Excel và callback (Consumer<Row>) để xử lý.
     * @param skipHeader số dòng tiêu đề cần bỏ qua (thường = 1)
     */
    public static void read(InputStream is, int skipHeader, Consumer<Row> rowConsumer) {
        try (Workbook wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getSheetAt(0);
            int rowIdx = 0;

            for (Row row : sheet) {
                // Bỏ qua header (tiêu đề cột)
                if (rowIdx++ < skipHeader) continue;

                // Gửi từng row cho service xử lý
                rowConsumer.accept(row);
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc file Excel", e);
        }
    }

    /** Tạo workbook mới để export */
    public static Workbook createWorkbook() {
        return new XSSFWorkbook();
    }

    /**
     * Tạo sheet + dòng header tự động.
     * Trả về sheet để caller tự ghi dữ liệu tiếp.
     */
    public static Sheet createSheetWithHeader(Workbook wb, String sheetName, String... headers) {
        Sheet sheet = wb.createSheet(sheetName);
        Row header = sheet.createRow(0);

        // Tạo cell header
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
            sheet.autoSizeColumn(i); // Tự căn độ rộng
        }

        return sheet;
    }
}
