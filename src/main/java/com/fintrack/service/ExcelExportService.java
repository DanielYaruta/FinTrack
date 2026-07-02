package com.fintrack.service;

import com.fintrack.model.Transaction;
import com.fintrack.model.TransactionType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Генерация Excel (.xlsx) с помощью Apache POI.
 *
 * Иерархия объектов POI:
 *   Workbook (файл) → Sheet (лист) → Row (строка) → Cell (ячейка)
 *
 * XSSFWorkbook — реализация для формата .xlsx (Open XML).
 * HSSFWorkbook — устаревший .xls (бинарный, Office 97-2003).
 *
 * Преимущество перед PDF: кириллица и все Unicode-символы
 * работают «из коробки» — POI использует Java String напрямую.
 */
@Service
public class ExcelExportService {

    public void export(List<Transaction> transactions, LocalDate from, LocalDate to,
                       OutputStream out) throws IOException {

        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            // ===== Стили =====
            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle incomeStyle = createAmountStyle(wb, new XSSFColor(new byte[]{25, (byte)135, 84}, null));
            CellStyle expenseStyle = createAmountStyle(wb, new XSSFColor(new byte[]{(byte)220, 53, 69}, null));
            CellStyle altRowStyle  = createAltRowStyle(wb);
            CellStyle numberStyle  = createNumberStyle(wb);
            CellStyle altNumberStyle = createAltNumberStyle(wb);

            // ===== Лист 1: Транзакции =====
            XSSFSheet sheet = wb.createSheet("Транзакции");
            sheet.setColumnWidth(0, 3000);  // Дата
            sheet.setColumnWidth(1, 8000);  // Описание
            sheet.setColumnWidth(2, 4000);  // Категория
            sheet.setColumnWidth(3, 3000);  // Тип
            sheet.setColumnWidth(4, 4000);  // Сумма

            int rowIdx = 0;

            // Заголовок файла
            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.setHeight((short) 600);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("FinTrack — Отчёт за период " + from + " — " + to);
            titleCell.setCellStyle(createTitleStyle(wb));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            rowIdx++; // пустая строка

            // Сводка
            BigDecimal income  = sum(transactions, TransactionType.INCOME);
            BigDecimal expense = sum(transactions, TransactionType.EXPENSE);
            BigDecimal balance = income.subtract(expense);

            writeLabel(sheet, rowIdx, wb, "Доходы:", income, incomeStyle);   rowIdx++;
            writeLabel(sheet, rowIdx, wb, "Расходы:", expense, expenseStyle); rowIdx++;
            writeLabel(sheet, rowIdx, wb, "Баланс:",  balance,
                    balance.signum() >= 0 ? incomeStyle : expenseStyle);      rowIdx++;
            writeLabel(sheet, rowIdx, wb, "Транзакций:", null, null);
            sheet.getRow(rowIdx).createCell(1).setCellValue(transactions.size()); rowIdx++;

            rowIdx++; // пустая строка

            // Шапка таблицы
            String[] headers = {"Дата", "Описание", "Категория", "Тип", "Сумма"};
            Row headerRow = sheet.createRow(rowIdx++);
            headerRow.setHeight((short) 450);
            for (int c = 0; c < headers.length; c++) {
                Cell cell = headerRow.createCell(c);
                cell.setCellValue(headers[c]);
                cell.setCellStyle(headerStyle);
            }

            // Закрепляем строку с заголовком таблицы (freeze pane)
            sheet.createFreezePane(0, rowIdx);

            // Данные
            for (Transaction t : transactions) {
                Row row = sheet.createRow(rowIdx++);
                boolean alt = rowIdx % 2 == 0;

                setCell(row, 0, t.getDate().toString(), alt ? altRowStyle : null);
                setCell(row, 1, nvl(t.getDescription(), ""), alt ? altRowStyle : null);
                setCell(row, 2, t.getCategory() != null ? t.getCategory().getName() : "", alt ? altRowStyle : null);
                setCell(row, 3, t.getType().name(), alt ? altRowStyle : null);

                // Числовая ячейка с суммой
                Cell amountCell = row.createCell(4);
                amountCell.setCellValue(t.getAmount().doubleValue());
                amountCell.setCellStyle(
                        t.getType() == TransactionType.INCOME
                                ? incomeStyle
                                : expenseStyle);
            }

            // ===== Лист 2: Сводка =====
            XSSFSheet summarySheet = wb.createSheet("Сводка");
            summarySheet.setColumnWidth(0, 5000);
            summarySheet.setColumnWidth(1, 5000);

            int si = 0;
            writeSummaryRow(summarySheet, si++, wb, "Доходы",    income,  incomeStyle);
            writeSummaryRow(summarySheet, si++, wb, "Расходы",   expense, expenseStyle);
            writeSummaryRow(summarySheet, si++, wb, "Баланс",    balance,
                    balance.signum() >= 0 ? incomeStyle : expenseStyle);

            wb.write(out);
        }
        // try-with-resources автоматически закрывает XSSFWorkbook
    }

    // --- Вспомогательные методы ---

    private BigDecimal sum(List<Transaction> txs, TransactionType type) {
        return txs.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String nvl(String v, String fallback) {
        return (v != null && !v.isBlank()) ? v : fallback;
    }

    private void setCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        if (style != null) cell.setCellStyle(style);
    }

    private void writeLabel(XSSFSheet sheet, int rowIdx, XSSFWorkbook wb,
                            String label, BigDecimal value, CellStyle style) {
        Row row = sheet.createRow(rowIdx);
        CellStyle labelStyle = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        labelStyle.setFont(font);

        Cell lc = row.createCell(0);
        lc.setCellValue(label);
        lc.setCellStyle(labelStyle);

        if (value != null) {
            Cell vc = row.createCell(1);
            vc.setCellValue(value.doubleValue());
            if (style != null) vc.setCellStyle(style);
        }
    }

    private void writeSummaryRow(XSSFSheet sheet, int rowIdx, XSSFWorkbook wb,
                                 String label, BigDecimal value, CellStyle style) {
        writeLabel(sheet, rowIdx, wb, label, value, style);
    }

    // --- Фабрики стилей ---

    private CellStyle createHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(new byte[]{52, 58, 64}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);

        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setColor(new XSSFColor(new byte[]{(byte)255, (byte)255, (byte)255}, null));
        style.setFont(font);
        return style;
    }

    private CellStyle createAmountStyle(XSSFWorkbook wb, XSSFColor color) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat fmt = wb.createDataFormat();
        style.setDataFormat(fmt.getFormat("#,##0.00"));
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setColor(color);
        style.setFont(font);
        return style;
    }

    private CellStyle createAltRowStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte)248, (byte)249, (byte)250}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createNumberStyle(XSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createAltNumberStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte)248, (byte)249, (byte)250}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createTitleStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}
