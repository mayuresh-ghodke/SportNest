package com.ecommerce.library.utils;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.ecommerce.library.model.Product;
import com.lowagie.text.Chunk;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class PdfGenerator {

    public static byte[] generateOrderReceiptPdf(String orderId, String customerName, 
    List<Product> productList, double total) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Header
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph header = new Paragraph("SPORTNEST - Order Receipt", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);
            document.add(Chunk.NEWLINE);

            // Order Info
            document.add(new Paragraph("Order ID: " + orderId));
            document.add(new Paragraph("Customer: " + customerName));
            document.add(new Paragraph(" "));

            // Product List
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.addCell("Product");
            table.addCell("Price");

            for (Product product : productList) {
                table.addCell(product.getName());              // Display product name
                table.addCell("₹" + product.getCostPrice());       // Display product price
            }

            document.add(table);
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Total: ₹" + total));
            document.close();

        } 
        catch (Exception e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }
}
