package edu.connexion3a77.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import edu.connexion3a77.entities.Document;
import edu.connexion3a77.entities.Ordonnance;
import edu.connexion3a77.entities.Rapport;

import java.awt.Color;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfService {

    // Palette de couleurs Premium
    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color SUCCESS = new Color(5, 150, 105);
    private static final Color GRAY_50 = new Color(248, 250, 252);
    private static final Color GRAY_100 = new Color(241, 245, 249);
    private static final Color GRAY_200 = new Color(226, 232, 240);
    private static final Color GRAY_500 = new Color(100, 116, 139);
    private static final Color GRAY_700 = new Color(51, 65, 85);
    private static final Color GRAY_800 = new Color(30, 41, 59);
    private static final Color GRAY_900 = new Color(15, 23, 42);
    private static final Color WHITE = Color.WHITE;

    public void generateMedicalDossier(Document doc, List<Rapport> rapports, List<Ordonnance> ordonnances,
            String filePath) throws Exception {
        com.lowagie.text.Document pdfDoc = new com.lowagie.text.Document(PageSize.A4, 50, 50, 70, 70);
        PdfWriter writer = PdfWriter.getInstance(pdfDoc, new FileOutputStream(filePath));
        writer.setPageEvent(new MedicalPageEvent());
        pdfDoc.open();

        // Polices
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, Font.NORMAL, WHITE);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, new Color(191, 219, 254));
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.NORMAL, GRAY_900);
        Font cardTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.NORMAL, GRAY_800);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Font.NORMAL, GRAY_700);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Font.NORMAL, GRAY_900);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.NORMAL, GRAY_500);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // ========== HEADER ==========
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(PRIMARY);
        headerCell.setPadding(30);
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.setCellEvent(new RoundedCellEvent(12));

        Paragraph t = new Paragraph("🏥 DOSSIER MÉDICAL", titleFont);
        t.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(t);

        Paragraph st = new Paragraph("Gestion complète des documents de santé", subtitleFont);
        st.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(st);

        headerTable.addCell(headerCell);
        pdfDoc.add(headerTable);

        pdfDoc.add(new Paragraph(" ", new Font(Font.HELVETICA, 20)));

        // ========== INFO DOCUMENT (CARTE) ==========
        pdfDoc.add(createSectionTitle("Informations du Document", PRIMARY, sectionFont));

        PdfPTable infoGrid = new PdfPTable(2);
        infoGrid.setWidthPercentage(100);
        infoGrid.setSpacingBefore(10);
        infoGrid.setWidths(new float[] { 1, 2.5f });

        addInfoRow(infoGrid, "Nom du fichier", doc.getNomFichier(), boldFont, normalFont);
        addInfoRow(infoGrid, "Type de dossier", doc.getType(), boldFont, normalFont);
        addInfoRow(infoGrid, "Date de création", doc.getDateCreation().toString(), boldFont, normalFont);
        if (doc.getDescription() != null) {
            addInfoRow(infoGrid, "Description", doc.getDescription(), boldFont, normalFont);
        }
        pdfDoc.add(infoGrid);

        // ========== RAPPORTS ==========
        if (rapports != null && !rapports.isEmpty()) {
            pdfDoc.add(new Paragraph(" ", new Font(Font.HELVETICA, 20)));
            pdfDoc.add(createSectionTitle("Rapports Médicaux (" + rapports.size() + ")", PRIMARY, sectionFont));

            for (Rapport r : rapports) {
                PdfPTable card = createCard(GRAY_50, GRAY_200);
                PdfPCell cell = card.getRow(0).getCells()[0];

                cell.addElement(new Paragraph("Motif : " + r.getConsultationReason(), boldFont));
                cell.addElement(new Paragraph("Diagnostic : " + r.getDiagnosis(), normalFont));
                if (r.getObservations() != null) {
                    cell.addElement(new Paragraph("Observations : " + r.getObservations(), smallFont));
                }
                pdfDoc.add(card);
            }
        }

        // ========== ORDONNANCES ==========
        if (ordonnances != null && !ordonnances.isEmpty()) {
            pdfDoc.add(new Paragraph(" ", new Font(Font.HELVETICA, 20)));
            pdfDoc.add(createSectionTitle("Ordonnances (" + ordonnances.size() + ")", SUCCESS, sectionFont));

            for (Ordonnance o : ordonnances) {
                PdfPTable card = createCard(new Color(240, 253, 244), new Color(187, 247, 208));
                PdfPCell cell = card.getRow(0).getCells()[0];

                cell.addElement(new Paragraph("Médicament : " + o.getMedicament(), boldFont));
                cell.addElement(new Paragraph("Posologie : " + o.getPosologie(), normalFont));
                cell.addElement(new Paragraph("Instructions : " + o.getInstructions(), smallFont));
                pdfDoc.add(card);
            }
        }

        pdfDoc.close();
    }

    private PdfPTable createSectionTitle(String text, Color color, Font font) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 0.02f, 0.98f });
        PdfPCell bar = new PdfPCell();
        bar.setBackgroundColor(color);
        bar.setBorder(Rectangle.NO_BORDER);
        PdfPCell tCell = new PdfPCell(new Phrase(text, font));
        tCell.setBorder(Rectangle.NO_BORDER);
        tCell.setPaddingLeft(12);
        table.addCell(bar);
        table.addCell(tCell);
        return table;
    }

    private PdfPTable createCard(Color bgColor, Color borderColor) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bgColor);
        cell.setBorderColor(borderColor);
        cell.setPadding(15);
        cell.setCellEvent(new RoundedCellEvent(8));
        table.addCell(cell);
        return table;
    }

    private void addInfoRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell l = new PdfPCell(new Phrase(label, labelFont));
        l.setBackgroundColor(GRAY_100);
        l.setPadding(8);
        l.setBorderColor(GRAY_200);
        table.addCell(l);
        PdfPCell v = new PdfPCell(new Phrase(value != null ? value : "N/A", valueFont));
        v.setPadding(8);
        v.setBorderColor(GRAY_200);
        table.addCell(v);
    }

    private static class RoundedCellEvent implements PdfPCellEvent {
        private int radius;

        public RoundedCellEvent(int r) {
            this.radius = r;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle pos, PdfContentByte[] canvases) {
            PdfContentByte cb = canvases[PdfPTable.BACKGROUNDCANVAS];
            cb.roundRectangle(pos.getLeft() + 1, pos.getBottom() + 1, pos.getWidth() - 2, pos.getHeight() - 2, radius);
            cb.stroke();
        }
    }

    private static class MedicalPageEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, com.lowagie.text.Document document) {
            PdfContentByte cb = writer.getDirectContent();
            cb.beginText();
            try {
                cb.setFontAndSize(BaseFont.createFont(), 9);
            } catch (Exception e) {
            }
            cb.setColorFill(GRAY_500);
            cb.showTextAligned(Element.ALIGN_CENTER, "Page " + writer.getPageNumber(),
                    (document.right() + document.left()) / 2, document.bottom() - 30, 0);
            cb.endText();
        }
    }
}
