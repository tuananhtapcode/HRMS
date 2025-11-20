package com.project.hrms.utils.excel;

import org.apache.poi.ss.usermodel.Cell;

public class ExcelCellParser {

    /** Safe String – tránh lỗi "cell type mismatch" */
    public static String getString(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK, _NONE -> null;
            default -> null;
        };
    }

    /** Safe Long – nếu lỗi sẽ trả null */
    public static Long getLong(Cell cell) {
        try {
            return cell == null ? null : (long) cell.getNumericCellValue();
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer getInt(Cell cell) {
        try {
            return cell == null ? null : (int) cell.getNumericCellValue();
        } catch (Exception e) {
            return null;
        }
    }

    public static Double getDouble(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Double.parseDouble(cell.getStringCellValue());
                } catch (Exception e) {
                    yield null;
                }
            }
            default -> null;
        };
    }


    public static Integer getInteger(Cell cell) {
        Double d = getDouble(cell);
        return d != null ? d.intValue() : null;
    }
}
