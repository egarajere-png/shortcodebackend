package com.abcbank.shortcode.shortcode.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.extern.slf4j.Slf4j;

import com.itextpdf.text.Image;

@Slf4j
public class SlipGenerator {
	
    private static String FILE;
    private static Font headingFont = new Font(Font.FontFamily.TIMES_ROMAN, 20,
            Font.BOLD, new BaseColor(0,0, 139));
    private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
            Font.UNDERLINE);
    private static Font blueFont = new Font(Font.FontFamily.TIMES_ROMAN, 12,
            Font.NORMAL, new BaseColor(0,0, 139));
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
		shortCodeData.put("sequenceNumber", "1");
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
            addGreetingsPage(document);
            createTable2(document);
            addInstructionsParagraph(document);
            createInstructionList(document);
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
        preface.add(new Paragraph("AFRICAN BANKING CORPORATION LTD", headingFont));        
        addEmptyLine(preface, 2);
        document.add(preface);
    }


    private static void addGreetingsPage(Document document)
            throws DocumentException {
        Paragraph preface = new Paragraph();
        addEmptyLine(preface, 1);
        preface.add(new Paragraph("Greetings from ABC Bank!", small));
        addEmptyLine(preface, 1);
        preface.add(new Paragraph("We are happy to notify you that your unique code has been generated and linked to your account number as below;", small));
        document.add(preface);
    }
    
    private static void addInstructionsParagraph(Document document)
            throws DocumentException {
        Paragraph preface = new Paragraph();
        addEmptyLine(preface, 1);
        preface.add(new Paragraph("You can now enjoy direct deposits into your account via Safaricom's Lipa na Mpesa.", small));
        addEmptyLine(preface, 1);
        preface.add(new Paragraph("To complete a transaction seamlessly, kindly follow the below steps;", small));
        document.add(preface);
    }
    
    private static void createInstructionList(Document document)
            throws BadElementException, DocumentException {
    	
    	Font zapfdingbats = new Font(FontFamily.ZAPFDINGBATS, 8);
    	//Chunk bullet = new Chunk(String.valueOf((char) 108), zapfdingbats);
        
    	Phrase bullet = new Phrase("*", smallBold);
    	Paragraph preface = new Paragraph();
    	preface.add(bullet);
    	preface.add(new Phrase(" Go to Safaricom’s", small));
    	preface.add(new Phrase(" SIM Tool Kit\n", smallBold));
    	preface.add(bullet);
    	preface.add(new Phrase(" Select", small));
    	preface.add(new Phrase(" MPESA", smallBold));
    	preface.add(new Phrase(" menu\n", small));
    	preface.add(bullet);
    	preface.add(new Phrase(" Select", small));
    	preface.add(new Phrase(" Lipa na MPESA\n", smallBold));
    	preface.add(bullet);
    	preface.add(new Phrase(" Select", small));
    	preface.add(new Phrase(" Pay bill\n", smallBold));
    	preface.add(bullet);
    	preface.add(new Phrase(" Select", small));
    	preface.add(new Phrase(" Business Number", smallBold));
    	preface.add(new Phrase(" and enter", small));
    	preface.add(new Phrase(" 111777\n", smallBold));
    	preface.add(bullet);
    	preface.add(new Phrase(" Select", small));
    	preface.add(new Phrase(" Account Number", smallBold));
    	preface.add(new Phrase(" and type", small));
    	preface.add(new Phrase(" " + shortCodeData.get("shortCode") + "\n", smallBold));
    	preface.add(bullet);
    	preface.add(new Phrase(" Enter", small));
    	preface.add(new Phrase(" Amount\n", smallBold));
    	preface.add(bullet);
    	preface.add(new Phrase(" Enter your", small));
    	preface.add(new Phrase(" MPESA pin number", smallBold));
    	preface.add(new Phrase(" to complete the transaction\n", small));
        document.add(preface);
    }
    
    private static void addFooterPage(Document document)
            throws DocumentException {
        Paragraph preface = new Paragraph();
        addEmptyLine(preface, 1);
        preface.add(new Paragraph("Should you have any issues/need any clarification, please contact us through", small));
        preface.add(new Paragraph(" Channels.Operations@abcthebank.com / talk2us@abcthebank.com", blueFont));
        preface.add(new Paragraph(" for support.", small));
        document.add(preface);
    }

    
    
    private static void createTable2(Document document)
            throws BadElementException, DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.getDefaultCell().setBorderWidth(0.1f);
        table.getDefaultCell().setPadding(2.0f);
        Paragraph preface = new Paragraph();
        addEmptyLine(preface, 1);
        table.addCell(new Paragraph("ACCOUNT NAME", small));
        table.addCell(new Paragraph(shortCodeData.get("accountName").toString(), smallBold));
        table.addCell(new Paragraph("ACCOUNT NUMBER", small));
        String accountNumber = shortCodeData.get("accountNumber").toString();
        accountNumber = accountNumber.substring(0,5) + "********" + accountNumber.substring(accountNumber.length() - 2, accountNumber.length());
        table.addCell(new Paragraph(accountNumber, smallBold));
        table.addCell(new Paragraph("UNIQUE CODE", small));
        table.addCell(new Paragraph(shortCodeData.get("shortCode").toString(), smallBold));
        table.addCell(new Paragraph("SHORT CODE SEQUENCE NO.", small));
        table.addCell(new Paragraph(shortCodeData.get("sequenceNumber").toString(), smallBold));
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
			image2.scaleAbsoluteHeight(90);
			image2.scaleAbsoluteWidth(120);
			image2.setAbsolutePosition(425f, 738f);
			document.add(image2);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
    }
}