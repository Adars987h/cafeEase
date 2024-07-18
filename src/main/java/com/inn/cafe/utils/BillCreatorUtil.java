package com.inn.cafe.utils;


import com.inn.cafe.POJO.Order;
import com.inn.cafe.constants.BillConstants;
import com.inn.cafe.constants.CafeConstants;
import com.inn.cafe.dto.OrderItem;
import com.inn.cafe.wrapper.CustomerWrapper;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.DashedBorder;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import lombok.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Data
public class BillCreatorUtil {
    Document document;
    PdfDocument pdfDocument;
    String pdfName;
    float threecol=190f;
    float twocol=285f;
    float twocol150=twocol+150f;
    float[] twocolumnWidth ={twocol150,twocol};
    float[] threeColumnWidth ={threecol,threecol,threecol};
    float[] itemTableWidth = {50f, 190f, 110f, 110f, 110f};
    float[] fullwidth ={threecol*3};

    public BillCreatorUtil setPdfName(String pdfName){
        this.pdfName = pdfName;
        return this;
    }

    public void createDocument() throws FileNotFoundException {
        String pdfFullPath= CafeConstants.STORE_LOCATION + pdfName;
        File file = new File(pdfFullPath);
        file.getParentFile().mkdirs();
        PdfWriter pdfWriter=new PdfWriter(pdfFullPath);
        pdfDocument=new PdfDocument(pdfWriter);
        pdfDocument.setDefaultPageSize(PageSize.A4);
        this.document=new Document(pdfDocument);
    }

    public void createCompanyHeader() {
        Table companyData=new Table(fullwidth);
        companyData.addCell(new Cell().add(BillConstants.COMPANY_HEADER).setFontSize(40f).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER).setBold());
        document.add(companyData);
        Border gb=new SolidBorder(Color.GRAY,2f);
        document.add(getDividerTable(fullwidth).setBorder(gb));
    }

    public void createInvoiceHeader(Order order) {
        Table invoiceData=new Table(twocolumnWidth);
        invoiceData.addCell(new Cell().add(BillConstants.INVOICE_TITLE).setFontSize(20f).setBorder(Border.NO_BORDER).setBold());

        Table nestedTable=new Table(new float[]{twocol/2,twocol/2});
        nestedTable.addCell(getHeaderTextCell(BillConstants.INVOICE_NO_TEXT));
        nestedTable.addCell(getHeaderTextCellValue(String.valueOf(order.getOrderId())));
        nestedTable.addCell(getHeaderTextCell(BillConstants.INVOICE_DATE_TEXT));
        nestedTable.addCell(getHeaderTextCellValue(order.getOrderDateAndTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
        invoiceData.addCell(new Cell().add(nestedTable).setBorder(Border.NO_BORDER));

        Border gb=new SolidBorder(Color.GRAY,2f);
        document.add(invoiceData);
        document.add(getDividerTable(fullwidth).setBorder(gb));
    }

    public void createCustomerInformation(CustomerWrapper customer) {
        Table twoColTable=new Table(twocolumnWidth);
        twoColTable.addCell(getCustomerInfoCell(BillConstants.CUSTOMER_INFO +" :").setUnderline());
        document.add(twoColTable.setMarginBottom(5f));

        Table customerInfoTable=new Table(threeColumnWidth);

        //ID
        customerInfoTable.addCell(getCell12fLeft(BillConstants.CUSTOMER_ID,true));
        customerInfoTable.addCell(getCell12fLeft(String.valueOf(customer.getId()),false));
        customerInfoTable.addCell(getCell12fLeft("",false));

        //NAME
        customerInfoTable.addCell(getCell12fLeft(BillConstants.CUSTOMER_NAME,true));
        customerInfoTable.addCell(getCell12fLeft(customer.getName(), false));
        customerInfoTable.addCell(getCell12fLeft("", false));

        //EMAIL
        customerInfoTable.addCell(getCell12fLeft(BillConstants.CUSTOMER_EMAIL,true));
        customerInfoTable.addCell(getCell12fLeft(String.valueOf(customer.getEmail()),false));
        customerInfoTable.addCell(getCell12fLeft("",false));

        //CONTACT NO
        customerInfoTable.addCell(getCell12fLeft(BillConstants.CUSTOMER_CONTACT_NO,true));
        customerInfoTable.addCell(getCell12fLeft(customer.getContactNumber(), false));
        customerInfoTable.addCell(getCell12fLeft("", false));

        document.add(customerInfoTable);
        document.add(new Table(threeColumnWidth));

        Border gb=new SolidBorder(Color.GRAY,2f);
        document.add(getDividerTable(fullwidth).setBorder(gb));
        document.add(getNewLineParagraph().setFontSize(5f));

    }

    public void createTableHeader() {
        Paragraph productPara=new Paragraph("Products :");
        document.add(productPara.setBold().setFontSize(15f));
        Table productTableHeader=new Table(itemTableWidth);
        productTableHeader.setBackgroundColor(Color.BLACK,0.7f);

        productTableHeader.addCell(new Cell().add(BillConstants.PRODUCT_TABLE_SERIAL_NO).setBold().setFontColor(Color.WHITE).setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER));
        productTableHeader.addCell(new Cell().add(BillConstants.PRODUCT_TABLE_DESCRIPTION).setBold().setFontColor(Color.WHITE).setBorder(Border.NO_BORDER));
        productTableHeader.addCell(new Cell().add(BillConstants.PRODUCT_TABLE_PRICE_PER_UNIT).setBold().setFontColor(Color.WHITE).setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER));
        productTableHeader.addCell(new Cell().add(BillConstants.PRODUCT_TABLE_QUANTITY).setBold().setFontColor(Color.WHITE).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
        productTableHeader.addCell(new Cell().add(BillConstants.PRODUCT_TABLE_PRICE).setBold().setFontColor(Color.WHITE).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER)).setMarginRight(15f);
        document.add(productTableHeader);
    }

    public void createItemsTable(Order order) {
        Table itmsTable=new Table(itemTableWidth);
        float totalSum=order.getTotalAmount();
        int i =1;
        int totalQuantity = 0;
        for (OrderItem item:order.getItems())
        {
            itmsTable.addCell(new Cell().add(String.valueOf(i)).setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER));
            itmsTable.addCell(new Cell().add(item.getProductName()).setBorder(Border.NO_BORDER));
            itmsTable.addCell(new Cell().add(String.valueOf(item.getPricePerUnit())).setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER));
            itmsTable.addCell(new Cell().add(String.valueOf(item.getQuantity())).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
            itmsTable.addCell(new Cell().add(String.valueOf(item.getPrice())).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER)).setMarginRight(15f);
            i++;
            totalQuantity += item.getQuantity();
        }

        document.add(itmsTable.setMarginBottom(20f));
        float onetwo[]={230f,340f};
        Table threeColTable4=new Table(onetwo);
        threeColTable4.addCell(new Cell().add("").setBorder(Border.NO_BORDER));
        threeColTable4.addCell(new Cell().add(fullWidthDashedBorder(fullwidth)).setBorder(Border.NO_BORDER));
        document.add(threeColTable4);


        Table totalRowTable=new Table(itemTableWidth);
        totalRowTable.addCell(new Cell().add("").setBorder(Border.NO_BORDER)).setMarginLeft(10f);
        totalRowTable.addCell(new Cell().add("").setBorder(Border.NO_BORDER)).setMarginLeft(10f);
        totalRowTable.addCell(new Cell().add("Total").setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER));
        totalRowTable.addCell(new Cell().add(String.valueOf(totalQuantity)).setTextAlignment(TextAlignment.CENTER).setBorder(Border.NO_BORDER));
        totalRowTable.addCell(new Cell().add(String.valueOf(totalSum)).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER)).setMarginRight(15f);

        document.add(totalRowTable);
        document.add(fullWidthDashedBorder(fullwidth));
        document.add(new Paragraph("\n"));
        document.add(getDividerTable(fullwidth).setBorder(new SolidBorder(Color.GRAY,1)).setMarginBottom(15f));
    }


    public void createTnc() throws MalformedURLException {
        List<String> tncList = getTermsAndConditions();
        String imagePath="src/main/resources/cafeEase_watermark.png";

        float footerY = document.getBottomMargin();
        Table tb = new Table(fullwidth);
        tb.addCell(new Cell().add("TERMS AND CONDITIONS\n").setBold().setBorder(Border.NO_BORDER));
        for (String tnc : tncList) {
            tb.addCell(new Cell().add(tnc).setFontSize(10f).setBorder(Border.NO_BORDER));
        }
        tb.setFixedPosition(40f, footerY, 530f);
        this.document.add(tb);

        ImageData imageData = ImageDataFactory.create(imagePath);
        Image image = new Image(imageData);
        float x = pdfDocument.getDefaultPageSize().getWidth() / 2;
        float y = pdfDocument.getDefaultPageSize().getHeight() / 2;
        image.setFixedPosition(x - 150, y - 200);
        image.setOpacity(0.2f);
        image.setAutoScale(true);
        document.add(image);

        document.close();
    }

    private static List<String> getTermsAndConditions() {
        List<String> tncList=new ArrayList<>();
        tncList.add("1. Payment is due upon receipt of this bill. Accepted payment methods include cash, credit/debit cards, and other electronic payment methods.");
        tncList.add("2. All sales are final. Refunds or exchanges are not provided for food and beverages. Contact us within 24 hours for any issues.");
        tncList.add("3. Consumption of food and beverages is at your own risk. Inform us of any allergies or dietary restrictions.");
        tncList.add("4. Present any promotional offers or discounts at the time of payment. Promotions and discounts cannot be combined unless stated.");
        tncList.add("5. Terms and conditions are subject to change without notice. Review regularly for updates.");
        return tncList;
    }

    private static  Table getDividerTable(float[] fullwidth) {
        return new Table(fullwidth);
    }

    private static Table fullWidthDashedBorder(float[] fullwidth) {
        Table tableDivider2=new Table(fullwidth);
        Border dgb=new DashedBorder(Color.GRAY,0.5f);
        tableDivider2.setBorder(dgb);
        return tableDivider2;
    }

    private static  Paragraph getNewLineParagraph() {
        return new Paragraph("\n");
    }

    private static Cell getHeaderTextCell(String textValue) {
        return new Cell().add(textValue).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
    }

    private static Cell getHeaderTextCellValue(String textValue) {
        return new Cell().add(textValue).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT);
    }

    private static Cell getCustomerInfoCell(String textValue) {
        return new Cell().add(textValue).setFontSize(12f).setBold().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT);
    }

    private static  Cell getCell12fLeft(String textValue, Boolean isBold){
        Cell myCell=new Cell().add(textValue).setFontSize(12f).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT);
        return  isBold ?myCell.setBold():myCell;
    }
}