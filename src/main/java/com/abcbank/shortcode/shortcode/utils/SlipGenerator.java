package com.abcbank.shortcode.shortcode.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.extern.slf4j.Slf4j;

import com.itextpdf.text.Image;

@Slf4j
public class SlipGenerator {
	
    private static String FILE;
    private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
            Font.UNDERLINE);
    private static Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.NORMAL, BaseColor.RED);
    private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16,
            Font.BOLD);
    private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.BOLD);
    
    private static Font tinyBold = new Font(Font.FontFamily.TIMES_ROMAN, 10,
            Font.BOLD);
    
    private static Font tiny = new Font(Font.FontFamily.TIMES_ROMAN, 10);
    
    private static Font smallItalic = new Font(Font.FontFamily.TIMES_ROMAN, 12,
    		Font.ITALIC);
            
    private static Font small = new Font(Font.FontFamily.TIMES_ROMAN, 12);
    private static Font tinyItalic = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.ITALIC);
    
    private static HashMap<String, Object> shortCodeData;

    public static void main(String[] args) {
		HashMap<String, Object> shortCodeData = new HashMap<String, Object>();
		shortCodeData.put("accountNumber", "001190001000062");
		shortCodeData.put("accountName", "Samuel Waithaka");
		shortCodeData.put("shortCode", "350001");
		generateShortCodeSlip(shortCodeData);
	}
    
    public static void generateShortCodeSlip(HashMap<String, Object> shortCodeData) {
    	SlipGenerator.shortCodeData = shortCodeData;
    	FILE = "/tmp/" + shortCodeData.get("shortCode") + ".pdf";
    	log.info("File path {}", FILE);
        try {
        	Document document = new Document();
            document.setMargins(75, 75, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(FILE));
            
            document.open();
            addMetaData(document);
            addImage(document);
            addTitlePage(document);
            createTable1(document);
            createTable2(document);
            drawLine(document);
            addFooterPage(document);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void addMetaData(Document document) {
        document.addTitle("ABC Bank DPF Document");
        document.addSubject("Short-code Slip");
        document.addKeywords("Short-code, ABC Bank, Slip");
        document.addAuthor("Samuel Waithaka");
        document.addCreator("Samuel Waithaka");
    }

    private static void addTitlePage(Document document)
            throws DocumentException {
        Paragraph preface = new Paragraph();
        addEmptyLine(preface, 2);
        preface.add(new Paragraph("ABC Bank Customer Account Shortcode", catFont));        
        addEmptyLine(preface, 2);
        document.add(preface);
    }


    private static void addFooterPage(Document document)
            throws DocumentException {
        Paragraph preface = new Paragraph();
        addEmptyLine(preface, 1);
        preface.add(new Paragraph("Mpesa paybill 111777, Account " + shortCodeData.get("shortCode"),small));
        document.add(preface);
    }

    private static void createTable1(Document document)
            throws BadElementException, DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.getDefaultCell().setBorderWidth(0.1f);
        table.getDefaultCell().setPadding(2.0f);
        table.addCell(new Paragraph("Shortcode: " + shortCodeData.get("shortCode"), tinyBold));
        table.addCell(new Paragraph("Generated on " + new Date(), tiny));
        document.add(table);
    }
    
    private static void createTable2(Document document)
            throws BadElementException, DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.getDefaultCell().setBorderWidth(0.1f);
        table.getDefaultCell().setPadding(2.0f);
        Paragraph preface = new Paragraph();
        addEmptyLine(preface, 1);
        table.addCell(new Paragraph("Account Number", small));
        table.addCell(new Paragraph(shortCodeData.get("accountNumber").toString(), smallBold));
        table.addCell(new Paragraph("Account Name", small));
        table.addCell(new Paragraph(shortCodeData.get("accountName").toString(), smallBold));
        table.addCell(new Paragraph("Paybill", small));
        table.addCell(new Paragraph("111777", smallBold));
        table.addCell(new Paragraph("Account", small));
        table.addCell(new Paragraph(shortCodeData.get("shortCode").toString(), smallBold));
        document.add(preface);
        document.add(table);
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
    
    private static void drawLine(Document document) throws DocumentException {
    	Paragraph preface = new Paragraph();
        preface.add(new Paragraph("__________________________________________________________________________", small));   
        document.add(preface);
    }
    
    private static void addImage(Document document) {
    	OutputStream outputStream;
		try {
			outputStream = new FileOutputStream(new File(FILE));
			PdfWriter.getInstance(document, outputStream);
			document.open();
			InputStream abcLogo = new ClassPathResource("images/abc-logo.png").getInputStream();
			Image image2 = Image.getInstance(FileCopyUtils.copyToByteArray(abcLogo));
			image2.scaleAbsoluteHeight(75);
			image2.scaleAbsoluteWidth(105);
			image2.setAbsolutePosition(250f, 738f);
			document.add(image2);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
    }
}