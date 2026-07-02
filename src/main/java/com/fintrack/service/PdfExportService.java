package com.fintrack.service;

import com.fintrack.model.Transaction;
import com.fintrack.model.TransactionType;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Генерация PDF с помощью iText 7.
 *
 * Структура iText 7:
 *   PdfWriter  → низкоуровневая запись байтов
 *   PdfDocument → модель PDF-файла (страницы, ресурсы)
 *   Document    → высокоуровневый API: параграфы, таблицы, стили
 *
 * Ограничение: стандартные шрифты PDF (Helvetica, Times) поддерживают только
 * Latin-1. Кириллица будет заменена знаками «?».
 * Решение для продакшна: вложить Unicode-шрифт (например, FreeSans.ttf) в ресурсы
 * и загрузить через PdfFontFactory.createFont("fonts/FreeSans.ttf", PdfEncodings.IDENTITY_H).
 */
@Service
public class PdfExportService {

    private static final DeviceRgb HEADER_BG   = new DeviceRgb(52,  58,  64);
    private static final DeviceRgb ROW_ALT_BG  = new DeviceRgb(248, 249, 250);
    private static final DeviceRgb INCOME_COLOR = new DeviceRgb(25,  135, 84);
    private static final DeviceRgb EXPENSE_COLOR = new DeviceRgb(220, 53,  69);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public void export(List<Transaction> transactions, LocalDate from, LocalDate to,
                       OutputStream out) throws IOException {

        PdfFont bold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        PdfDocument pdf = new PdfDocument(new PdfWriter(out));
        Document doc = new Document(pdf);
        doc.setMargins(36, 36, 36, 36);

        // --- Заголовок ---
        doc.add(new Paragraph("FinTrack Report")
                .setFont(bold).setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph("Period: " + from.format(DATE_FMT) + " — " + to.format(DATE_FMT))
                .setFont(normal).setFontSize(10)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(16));

        // --- Сводка ---
        BigDecimal income  = sum(transactions, TransactionType.INCOME);
        BigDecimal expense = sum(transactions, TransactionType.EXPENSE);
        BigDecimal balance = income.subtract(expense);

        Table summary = new Table(UnitValue.createPercentArray(new float[]{3, 3, 3}))
                .useAllAvailableWidth().setMarginBottom(20);

        addSummaryCell(summary, "Income",  income,  INCOME_COLOR,  bold, normal);
        addSummaryCell(summary, "Expense", expense, EXPENSE_COLOR, bold, normal);
        addSummaryCell(summary, "Balance", balance,
                balance.signum() >= 0 ? INCOME_COLOR : EXPENSE_COLOR, bold, normal);
        doc.add(summary);

        // --- Таблица транзакций ---
        doc.add(new Paragraph("Transactions (" + transactions.size() + ")")
                .setFont(bold).setFontSize(12).setMarginBottom(6));

        float[] cols = {1.5f, 3f, 2f, 1.5f, 2f};
        Table table = new Table(UnitValue.createPercentArray(cols)).useAllAvailableWidth();

        addHeaderCell(table, "Date",        bold);
        addHeaderCell(table, "Description", bold);
        addHeaderCell(table, "Category",    bold);
        addHeaderCell(table, "Type",        bold);
        addHeaderCell(table, "Amount",      bold);

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            boolean even = i % 2 == 0;

            addDataCell(table, t.getDate().format(DATE_FMT), normal, even, TextAlignment.LEFT);
            addDataCell(table, nvl(t.getDescription(), "—"), normal, even, TextAlignment.LEFT);
            addDataCell(table, t.getCategory() != null ? t.getCategory().getName() : "—",
                    normal, even, TextAlignment.LEFT);
            addDataCell(table, t.getType().name(), normal, even, TextAlignment.CENTER);

            // Сумма окрашивается по типу транзакции
            String amtStr = (t.getType() == TransactionType.INCOME ? "+" : "-")
                    + " " + t.getAmount();
            DeviceRgb amtColor = t.getType() == TransactionType.INCOME ? INCOME_COLOR : EXPENSE_COLOR;
            table.addCell(new Cell()
                    .add(new Paragraph(amtStr).setFont(bold).setFontColor(amtColor))
                    .setBackgroundColor(even ? ROW_ALT_BG : ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setPadding(5).setBorder(new SolidBorder(new DeviceRgb(222, 226, 230), 0.5f)));
        }

        doc.add(table);

        // --- Футер ---
        doc.add(new Paragraph("Generated by FinTrack")
                .setFont(normal).setFontSize(8)
                .setFontColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(20));

        doc.close();
    }

    // --- Вспомогательные методы ---

    private void addHeaderCell(Table table, String text, PdfFont font) {
        table.addHeaderCell(new Cell()
                .add(new Paragraph(text).setFont(font).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(HEADER_BG)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6));
    }

    private void addDataCell(Table table, String text, PdfFont font,
                             boolean altRow, TextAlignment align) {
        table.addCell(new Cell()
                .add(new Paragraph(text).setFont(font))
                .setBackgroundColor(altRow ? ROW_ALT_BG : ColorConstants.WHITE)
                .setTextAlignment(align)
                .setPadding(5)
                .setBorder(new SolidBorder(new DeviceRgb(222, 226, 230), 0.5f)));
    }

    private void addSummaryCell(Table table, String label, BigDecimal value,
                                DeviceRgb color, PdfFont bold, PdfFont normal) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setFont(normal).setFontSize(9)
                        .setFontColor(ColorConstants.GRAY))
                .add(new Paragraph(value.toString()).setFont(bold).setFontSize(14)
                        .setFontColor(color))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(10));
    }

    private BigDecimal sum(List<Transaction> txs, TransactionType type) {
        return txs.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String nvl(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }
}
