package com.pidev.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

public class ExportUtils {

    public static <T> void exportToCSV(Window owner, String fileName, List<T> items,
                                       String[] headers, Function<T, String[]> rowMapper) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV");
        fileChooser.setInitialFileName(fileName + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(owner);
        if (file == null) return;

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.println(String.join(",", headers));
            for (T item : items) {
                writer.println(String.join(",", rowMapper.apply(item)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> void exportToPDF(Window owner, String title, List<T> items,
                                       String[] headers, Function<T, String[]> rowMapper) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF");
        fileChooser.setInitialFileName(title.replace(" ", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(owner);
        if (file == null) return;

        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLUE);
            Paragraph titlePara = new Paragraph(title, titleFont);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            titlePara.setSpacingAfter(20);
            document.add(titlePara);

            // Table
            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            // Header
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new BaseColor(59, 130, 246));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                table.addCell(cell);
            }

            // Data rows
            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            for (T item : items) {
                String[] row = rowMapper.apply(item);
                for (String cellValue : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(cellValue, dataFont));
                    cell.setPadding(6);
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}