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
import com.inn.cafe.dto.TopSellerResponse;
import com.inn.cafe.enums.OrderStatus;
import com.inn.cafe.constants.CafeConstants;
import com.inn.cafe.exceptions.BadRequestException;
import com.inn.cafe.exceptions.NotFoundException;
import com.inn.cafe.exceptions.UnauthorizedException;
import com.inn.cafe.service.CartService;
import com.inn.cafe.service.OrderService;
import com.inn.cafe.wrapper.ProductWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()){
                throw new BadRequestException("Your cart is empty. Add something before placing an order.");
            }
            Order order = new Order();
            order.setCustomer(user);
            order.setItems(cart.getItems());
            float tax = Math.round(cart.getTotalAmount() * CafeConstants.TAX_RATE * 100f) / 100f;
            order.setTaxAmount(tax);
            order.setTotalAmount(cart.getTotalAmount() + tax);
            order.setTotalQuantity(cart.getTotalQuantity());
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
                customerFromRequest=orderSearchRequest.getCustomer();
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
    public Order cancelOrder(Integer orderId) {
        Order order = getVisibleOrder(orderId);
        if (!jwtFilter.isAdmin() && order.getOrderStatus() != OrderStatus.ORDER_PLACED) {
            throw new BadRequestException("The kitchen has already started this order, so it can't be cancelled here. Ask at the counter.");
        }
        return transition(order, OrderStatus.CANCELLED);
    }

    @Override
    public Order updateStatus(Integer orderId, String status) {
        if (!jwtFilter.isAdmin()) {
            throw new UnauthorizedException(CafeConstants.UNAUTHORISED_ACCESS);
        }
        OrderStatus next;
        try {
            next = OrderStatus.valueOf(String.valueOf(status).trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unknown status: " + status);
        }
        return transition(getVisibleOrder(orderId), next);
    }

    private Order transition(Order order, OrderStatus next) {
        OrderStatus current = order.getOrderStatus();
        if (current == null || !current.canMoveTo(next)) {
            throw new BadRequestException("Order " + order.getOrderId() + " is " + current + " and can't move to " + next + ".");
        }
        order.setOrderStatus(next);
        return orderDao.save(order);
    }

    @Override
    public Order getVisibleOrder(Integer orderId) {
        Order order = orderId == null ? null : orderDao.findByOrderId(orderId);
        User current = customerUserDetailsService.getUserDetail();
        boolean visible = order != null && current != null
                && (jwtFilter.isAdmin() || Objects.equals(order.getCustomer().getId(), current.getId()));
        // Same response for "doesn't exist" and "isn't yours", so order ids can't be probed.
        if (!visible) {
            throw new NotFoundException("Order " + orderId + " was not found.");
        }
        return order;
    }

    @Override
    public Order orderByAdmin(User user, List<OrderItem> items) {
        if (!jwtFilter.isAdmin()) {
            // Any signed-in customer could previously place orders under any email.
            throw new UnauthorizedException(CafeConstants.UNAUTHORISED_ACCESS);
        }
        try{
            HashMap<Integer, Integer> productIdToQuantityMap = new HashMap<>();
            List<Integer> productIds = new ArrayList<>();
            for (OrderItem item : items) {
                productIds.add(item.getProductId());
                productIdToQuantityMap.put(item.getProductId(), item.getQuantity());
            }

            List<ProductWrapper> products = productDao.getProductsWithProductIdIn(productIds);

            float totalPrice=0;
            int totalQuantity =0;

            List<OrderItem> orderedItems = new ArrayList<>();
            for (ProductWrapper product:products){
                OrderItem item= new OrderItem();
                item.setProductId(product.getId());
                item.setProductName(product.getName());
                item.setQuantity(productIdToQuantityMap.get(product.getId()));
                item.setPricePerUnit(product.getPrice());
                item.setPrice(productIdToQuantityMap.get(product.getId())*product.getPrice());

                orderedItems.add(item);

                totalQuantity += item.getQuantity();
                totalPrice+=item.getPrice();
            }

            Order order =new Order();
            order.setCustomer(user);
            order.setItems(orderedItems);
            float tax = Math.round(totalPrice * CafeConstants.TAX_RATE * 100f) / 100f;
            order.setTaxAmount(tax);
            order.setTotalAmount(totalPrice + tax);
            order.setTotalQuantity(totalQuantity);
            order.setOrderDateAndTime(LocalDateTime.now());
            order.setOrderStatus(OrderStatus.ORDER_PLACED);

            return orderDao.save(order);
        }catch(Exception ex){
            log.error(ex.getMessage());
            throw ex;
        }
    }

    @Override
    public TopSellerResponse getTopSellerToday() {
        try {
            LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            List<Order> todaysOrders = orderDao.findByTimeAndOrderId(startOfDay, endOfDay, null);

            // Items are stored as a JSON blob per order (see Order.itemsJson), not a
            // normalised join table, so there is no SQL GROUP BY to reach for here --
            // aggregate the already-fetched orders in memory. Fine at this catalogue
            // and order-volume scale; would need a real aggregate query if either grew
            // substantially.
            Map<Integer, TopSellerResponse> totals = new HashMap<>();
            for (Order order : todaysOrders) {
                for (OrderItem item : order.getItems()) {
                    TopSellerResponse running = totals.get(item.getProductId());
                    if (running == null) {
                        totals.put(item.getProductId(), new TopSellerResponse(
                                item.getProductId(), item.getProductName(), item.getPricePerUnit(), item.getQuantity()));
                    } else {
                        running.setQuantitySold(running.getQuantitySold() + item.getQuantity());
                    }
                }
            }

            return totals.values().stream()
                    .max(Comparator.comparingInt(TopSellerResponse::getQuantitySold))
                    .orElse(null);
        } catch (Exception ex) {
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
