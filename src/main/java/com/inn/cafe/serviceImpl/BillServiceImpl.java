package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Order;
import com.inn.cafe.constants.CafeConstants;
import com.inn.cafe.dao.BillDao;
import com.inn.cafe.dto.BillResponse;
import com.inn.cafe.service.BillService;
import com.inn.cafe.utils.BillCreatorUtil;
import com.inn.cafe.wrapper.CustomerWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Service
@Slf4j
public class BillServiceImpl implements BillService {

    @Autowired
    BillCreatorUtil billCreatorUtil;

    @SneakyThrows
    @Override
    public Boolean generateBill(CustomerWrapper customer, Order order) {
        try {
            log.info("Started Generating Bill");
            String pdfName= getPdfName(order.getOrderId());
            billCreatorUtil = billCreatorUtil.setPdfName(pdfName);
            billCreatorUtil.createDocument();

            // Header
            billCreatorUtil.createCompanyHeader();
            billCreatorUtil.createInvoiceHeader(order);

            // Customer Information
            billCreatorUtil.createCustomerInformation(customer);

            //Items
            billCreatorUtil.createTableHeader();
            billCreatorUtil.createItemsTable(order);

            //Term and Condition
            billCreatorUtil.createTnc();
            log.info("Bill Generation Completed");
            return true;
        }catch (Exception ex){
            log.info("Bill Generation Failed");
            log.error(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public BillResponse getBillStream(Integer orderId) throws IOException {
        try {
            log.info("Started Generating Stream for Bill");
            String pdfName = getPdfName(orderId);
            // Path to the PDF file
            File file = new File(CafeConstants.STORE_LOCATION + pdfName);

            // Create an InputStream from the file
            FileInputStream fileInputStream = new FileInputStream(file);

            // Create InputStreamResource from FileInputStream
            InputStreamResource billResource = new InputStreamResource(fileInputStream);
//            fileInputStream.close();
            log.info("Completed Generating Stream for Bill");
            return new BillResponse(billResource, file);
        }catch(Exception ex){
            log.error(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void deleteBill(Integer orderId) throws IOException {
        log.info("Started Deleting Bill");
        String pdfName = getPdfName(orderId);
        // Path to the PDF file
        Path path = Paths.get(CafeConstants.STORE_LOCATION + pdfName);
        Files.delete(path);
        log.info("Completed Deleting Bill");
    }


    private String getPdfName(Integer orderId){
        return "Invoice-"+orderId+".pdf";
    }
}
