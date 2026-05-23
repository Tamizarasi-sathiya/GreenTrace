package com.greentrace.greentrace.service;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class CertificateService {

    public byte[] generateCertificate(String company, int credits){

        try{

            PDDocument document = new PDDocument();
            PDPage page = new PDPage();

            document.addPage(page);

            PDPageContentStream content =
                    new PDPageContentStream(document,page);

            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD,26);
            content.newLineAtOffset(100,700);
            content.showText("GreenTrace Carbon Offset Certificate");
            content.endText();

            content.beginText();
            content.setFont(PDType1Font.HELVETICA,16);
            content.newLineAtOffset(100,650);
            content.showText("This certifies that");
            content.endText();

            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD,20);
            content.newLineAtOffset(100,620);
            content.showText(company);
            content.endText();

            content.beginText();
            content.setFont(PDType1Font.HELVETICA,16);
            content.newLineAtOffset(100,590);
            content.showText("has offset");
            content.endText();

            content.beginText();
            content.setFont(PDType1Font.HELVETICA_BOLD,18);
            content.newLineAtOffset(100,560);
            content.showText(credits + " Carbon Credits");
            content.endText();

            content.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            document.close();

            return out.toByteArray();

        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }
}