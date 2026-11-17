
package com.company.exportautomation.service;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.springframework.stereotype.Service;

import com.company.exportautomation.entity.Buyer;
import com.company.exportautomation.repository.BuyerRepository;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class BuyerPdfService {

    private final BuyerRepository buyerRepository;

    public BuyerPdfService(BuyerRepository buyerRepository) {
        this.buyerRepository = buyerRepository;
    }

    public byte[] generateBuyerPdf() throws Exception {

        List<Buyer> buyers = buyerRepository.findAll();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Document document = new Document();

        PdfWriter.getInstance(document, outputStream);

        document.open();

        Font titleFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 18);

        Paragraph title = new Paragraph(
                "API Export - Buyer Report", titleFont);

        title.setAlignment(Element.ALIGN_CENTER);

        document.add(title);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Total Buyers: " + buyers.size()));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);

        String[] headers = {
                "ID", "Company", "Email", "Country", "Buyer Type", "Website"
        };

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header));
            table.addCell(cell);
        }

        for (Buyer buyer : buyers) {

            table.addCell(String.valueOf(buyer.getId()));

            table.addCell(
                    buyer.getCompanyName() == null ? "-" : buyer.getCompanyName());

            table.addCell(
                    buyer.getEmail() == null ? "-" : buyer.getEmail());

            table.addCell(
                    buyer.getCountry() == null ? "-" : buyer.getCountry());

            table.addCell(
                    buyer.getBuyerType() == null ? "-" : buyer.getBuyerType());

            table.addCell(
                    buyer.getWebsite() == null ? "-" : buyer.getWebsite());
        }

        document.add(table);

        document.close();

        return outputStream.toByteArray();
    }
}
