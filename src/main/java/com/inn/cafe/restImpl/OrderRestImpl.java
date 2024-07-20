package com.inn.cafe.restImpl;

import com.inn.cafe.POJO.Order;
import com.inn.cafe.POJO.User;
import com.inn.cafe.constants.BillConstants;
import com.inn.cafe.dto.OrderItem;
import com.inn.cafe.dto.OrderSearchRequest;
import com.inn.cafe.dto.Response;
import com.inn.cafe.rest.OrderRest;
import com.inn.cafe.service.OrderService;
import com.inn.cafe.service.UserService;
import com.inn.cafe.utils.EmailUtils;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
@RestController
public class OrderRestImpl implements OrderRest {

    @Autowired
    OrderService orderService;

    @Autowired
    UserService userService;

    @Autowired
    EmailUtils emailUtils;

    @Override
    public ResponseEntity<Response> placeOrder() {
        try{
            Order order = orderService.placeOrder();
            sendOrderDetailsToUser(order);
            return new ResponseEntity<>(new Response(order), HttpStatus.OK);
        }catch (Exception ex){
            log.error(ex.getMessage());
            ex.printStackTrace();
            return new ResponseEntity<>(new Response(ex.getMessage()),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Response> getOrders(OrderSearchRequest orderSearchRequest) {
        try{
            List<Order> orders = orderService.searchOrders(orderSearchRequest);
            Response response = new Response(orders);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception ex){
            log.error(ex.getMessage());
            ex.printStackTrace();
            return new ResponseEntity<>(new Response(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public ResponseEntity<Response> cancelOrder(Integer orderId) {
        try{
            String result=orderService.cancelOrder(orderId);
            Response response = new Response(result);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch(Exception ex){
            log.error(ex.getMessage());

            return new ResponseEntity<>(new Response(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Response> orderByAdmin(String emailId, List<OrderItem> items) {
        try{
            User user= userService.findByEmail(emailId);
            
            Order order= orderService.orderByAdmin(user,items);
            sendOrderDetailsToUser(order);
            Response response = new Response(order);
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        }catch (Exception ex){
            log.error(ex.getMessage());

            return  new ResponseEntity<>(new Response(ex.getMessage()),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void sendOrderDetailsToUser(Order order) throws MessagingException {
        String emailTo = order.getCustomer().getEmail();
        String subject = "Order Placed - CafeEase";
//        System.out.println(order);
//        System.out.println(getOrderHtml(order));
        emailUtils.sendHtmlMessage(emailTo, subject, getOrderHtml(order), null);
    }

    private String getOrderHtml(Order order) {
        StringBuilder productInformation = new StringBuilder();
        productInformation.append("<table border='1' border-collapse=collapse>")
                .append("<tr><th>S No.</th><th>Description</th><th>Price Per Unit</th><th>Quantity</th><th>Price</th></tr>");
        for (int i = 1; i <= order.getItems().size(); i++) {
            OrderItem item = order.getItems().get(i - 1);
            productInformation.append("<tr>")
                    .append("<td>").append(i).append("</td>")
                    .append("<td>").append(item.getProductName()).append("</td>")
                    .append("<td>").append(item.getPricePerUnit()).append("</td>")
                    .append("<td>").append(item.getQuantity()).append("</td>")
                    .append("<td>").append(item.getPrice()).append("</td>")
                    .append("</tr>");
        }
//        productInformation.append("</table>");
        // Add total quantity and total price row
        productInformation.append("<tr>")
                .append("<td colspan='3'><strong>Total</strong></td>")
                .append("<td>").append(order.getTotalQuantity()).append("</td>")
                .append("<td>").append(order.getTotalAmount()).append("</td>")
                .append("</tr>")
                .append("</table>");



        return "<h3>Your order is placed with CafeEase<br></h3>" +
                "<h5> Details:</h5>" +
                "<p>Order Id - " + order.getOrderId() + "</p>" +
                "<p>Order Date - " + order.getOrderDateAndTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "</p>" +
                "<h4>Customer Information:</h4>" +
                "<p>" + BillConstants.CUSTOMER_ID + " - " + order.getCustomer().getId() + "</p>" +
                "<p>" + BillConstants.CUSTOMER_NAME + " - " + order.getCustomer().getName() + "</p>" +
                "<p>" + BillConstants.CUSTOMER_EMAIL + " - " + order.getCustomer().getEmail() + "</p>" +
                "<p>" + BillConstants.CUSTOMER_CONTACT_NO + " - " + order.getCustomer().getContactNumber() + "</p>" +
                "<h4>Product Details:</h4>" + productInformation +
                "<br> Download your bill by loging in to our website.";
    }

}
