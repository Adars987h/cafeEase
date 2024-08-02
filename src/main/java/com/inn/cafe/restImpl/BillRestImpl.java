package com.inn.cafe.restImpl;

import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Order;
import com.inn.cafe.POJO.User;
import com.inn.cafe.dto.BillResponse;
import com.inn.cafe.dto.OrderSearchRequest;
import com.inn.cafe.exceptions.BadRequestException;
import com.inn.cafe.rest.BillRest;
import com.inn.cafe.service.BillService;
import com.inn.cafe.service.OrderService;
import com.inn.cafe.wrapper.CustomerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;


@RestController
@Slf4j
public class BillRestImpl implements BillRest {

    @Autowired
    BillService billService;

    @Autowired
    OrderService orderService;

    @Autowired
    CustomerUserDetailsService customerUserDetailsService;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<InputStreamResource> downloadBill(Integer orderId) throws IOException {
        try{
            User user = customerUserDetailsService.getUserDetail();
            OrderSearchRequest orderSearchRequest = new OrderSearchRequest();

            if (jwtFilter.isUser())
                orderSearchRequest.setCustomer(user);
            orderSearchRequest.setOrderId(orderId);

            List<Order> orders = orderService.searchOrders(orderSearchRequest);
            if (!CollectionUtils.isEmpty(orders)){
                Boolean isBillGenerated = billService.generateBill(new CustomerWrapper(user), orders.get(0));
                if (isBillGenerated){
                    BillResponse billResponse = billService.getBillStream(orderId);
                    String fileName = billResponse.getFile().getName();
                    long contentLength = billResponse.getFile().length();

                    // Schedule the file for deletion after the response is sent
                    new Thread(() -> {
                        try {
                            // Sleep to ensure the file is no longer in use
                            Thread.sleep(30000);
                            billService.deleteBill(orderId);
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();

                    // Set the content type and headers
                    HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

                    return ResponseEntity.ok()
                            .headers(headers)
                            .contentLength(contentLength)
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(billResponse.getInputStreamResource());
                }
            }
            throw new BadRequestException("Please enter a Valid Order Id !!!!!");
        }catch(Exception ex){
            log.error(ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

}


