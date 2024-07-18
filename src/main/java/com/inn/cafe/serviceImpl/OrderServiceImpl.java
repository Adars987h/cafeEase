package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Cart;
import com.inn.cafe.POJO.Order;
import com.inn.cafe.POJO.User;
import com.inn.cafe.dao.CartDao;
import com.inn.cafe.dao.OrderDao;
import com.inn.cafe.dao.ProductDao;
import com.inn.cafe.dto.OrderItem;
import com.inn.cafe.dto.OrderSearchRequest;
import com.inn.cafe.enums.OrderStatus;
import com.inn.cafe.exceptions.BadRequestException;
import com.inn.cafe.service.CartService;
import com.inn.cafe.service.OrderService;
import com.inn.cafe.wrapper.ProductWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderDao orderDao;

    @Autowired
    CartDao cartDao;
    
    @Autowired
    ProductDao productDao;

    @Autowired
    CartService cartService;

    @Autowired
    CustomerUserDetailsService customerUserDetailsService;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public Order placeOrder() {
        try {
            User user = customerUserDetailsService.getUserDetail();
            Cart cart = cartDao.getCartByCustomerId(user);
            if (cart.getItems().isEmpty()){
                throw new RuntimeException("Cart Is Empty, Please add items to cart for placing order");
            }
            Order order = new Order();
            order.setCustomer(user);
            order.setItems(cart.getItems());
            order.setTotalAmount(cart.getTotalAmount());
            order.setOrderDateAndTime(LocalDateTime.now());
            order.setOrderStatus(OrderStatus.ORDER_PLACED);

            Order placedOrder = orderDao.save(order);
            cartService.emptyCart();
            return placedOrder;

        } catch (Exception ex) {
            throw ex;
        }

    }

    @Override
    public List<Order> searchOrders(OrderSearchRequest orderSearchRequest) {
        try {
            boolean isUser = jwtFilter.isUser();
            User currentUser = customerUserDetailsService.getUserDetail();
            User customerFromRequest = orderSearchRequest.getCustomer();
            if (isUser){
                if (Objects.nonNull(customerFromRequest)){
                    validateOrderSearchRequestCustomer(customerFromRequest, currentUser);
                }

                orderSearchRequest.setCustomer(currentUser);
            }
            List<Order> orders = new ArrayList<>();
            if (Objects.isNull(orderSearchRequest.getOrderId())){
                orders = orderDao.findByUserAndTime(customerFromRequest, orderSearchRequest.getStartTime(), orderSearchRequest.getEndTime());
            }else {
                if (Objects.nonNull(customerFromRequest) && Objects.nonNull(orderSearchRequest.getOrderId())){
                    orders = orderDao.findByUserTimeAndOrderId(customerFromRequest, orderSearchRequest.getStartTime(), orderSearchRequest.getEndTime(), orderSearchRequest.getOrderId());
                } else {
                    if (!isUser){
                        orders = orderDao.findByTimeAndOrderId(orderSearchRequest.getStartTime(), orderSearchRequest.getEndTime(), orderSearchRequest.getOrderId());
                    }
                }
            }
            return orders;
        }catch (Exception ex){
            throw ex;
        }
    }

    @Override
    public String cancelOrder(Integer orderId) {
        try{
            Order order = orderDao.findByOrderId(orderId);
            User currentUser = customerUserDetailsService.getUserDetail();
            if (Objects.nonNull(order)){
                if (jwtFilter.isAdmin()||(jwtFilter.isUser()&& Objects.equals(order.getCustomer().getId(), currentUser.getId()))){
                    orderDao.deleteById(orderId);

                    return "Order : "+orderId+" deleted Successfully...";
                }
                throw new BadRequestException("Please enter a Valid Order Id !!!!!");
            }
            throw new BadRequestException("Please enter a Valid Order Id !!!!!");
        }catch (Exception ex){
            log.error(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public Order orderByAdmin(User user, List<OrderItem> items) {
        try{
            HashMap<Integer, Integer> productIdToQuantityMap = new HashMap<>();
            List<Integer> productIds = new ArrayList<>();
            for (OrderItem item : items) {
                productIds.add(item.getProductId());
                productIdToQuantityMap.put(item.getProductId(), item.getQuantity());
            }

            List<ProductWrapper> products = productDao.getProductsWithProductIdIn(productIds);

            float totalPrice=0;

            List<OrderItem> orderedItems = new ArrayList<>();
            for (ProductWrapper product:products){
                OrderItem item= new OrderItem();
                item.setProductId(product.getId());
                item.setProductName(product.getName());
                item.setQuantity(productIdToQuantityMap.get(product.getId()));
                item.setPricePerUnit(product.getPrice());
                item.setPrice(productIdToQuantityMap.get(product.getId())*product.getPrice());

                orderedItems.add(item);

                totalPrice+=item.getPrice();
            }

            Order order =new Order();
            order.setCustomer(user);
            order.setItems(orderedItems);
            order.setTotalAmount(totalPrice);
            order.setOrderDateAndTime(LocalDateTime.now());
            order.setOrderStatus(OrderStatus.ORDER_PLACED);

            return orderDao.save(order);
        }catch(Exception ex){
            log.error(ex.getMessage());
            throw ex;
        }
    }

    private static void validateOrderSearchRequestCustomer(User customerFromRequest, User currentUser) {
        if (Objects.nonNull(customerFromRequest.getEmail()) && !(customerFromRequest.getEmail().equalsIgnoreCase(currentUser.getEmail())))
            throw new RuntimeException("Insufficient Privilege: User cannot search for other users");
        if (Objects.nonNull(customerFromRequest.getId()) && (!customerFromRequest.getId().equals(currentUser.getId())))
            throw new RuntimeException("Insufficient Privilege: User cannot search for other users");
        if (Objects.nonNull(customerFromRequest.getName()) && !(customerFromRequest.getName().equalsIgnoreCase(currentUser.getName())))
            throw new RuntimeException("Insufficient Privilege: User cannot search for other users");
        if (Objects.nonNull(customerFromRequest.getContactNumber()) && !(customerFromRequest.getContactNumber().equalsIgnoreCase(currentUser.getContactNumber())))
            throw new RuntimeException("Insufficient Privilege: User cannot search for other users");
    }
}
