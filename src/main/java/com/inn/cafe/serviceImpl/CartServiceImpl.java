package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.POJO.Cart;
import com.inn.cafe.POJO.User;
import com.inn.cafe.dao.CartDao;
import com.inn.cafe.dao.ProductDao;
import com.inn.cafe.dto.OrderItem;
import com.inn.cafe.exceptions.BadRequestException;
import com.inn.cafe.service.CartService;
import com.inn.cafe.service.UserService;
import com.inn.cafe.wrapper.ProductWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartDao cartDao;

    @Autowired
    ProductDao productDao;

    @Autowired
    CustomerUserDetailsService customerUserDetailsService;

    @Autowired
    UserService userService;

    @Override
    public Cart addOrUpdateItem(List<OrderItem> items) {
        try {

            User user = customerUserDetailsService.getUserDetail();
            Cart cart = cartDao.findByCustomerId(user);

            Cart updated = setCartItemsAndTotalPrice(
                    Objects.isNull(cart) ? new ArrayList<>() : cart.getItems(), items);
            // Update the loaded cart in place. Saving a second Cart object with the same id
            // merged onto the loaded one, whose @PreUpdate re-serialised its own stale item
            // list over the change.
            if (Objects.isNull(cart)) {
                cart = new Cart();
                cart.setCustomerId(user);
            }
            cart.setItems(updated.getItems());
            cart.setTotalAmount(updated.getTotalAmount());
            cart.setTotalQuantity(updated.getTotalQuantity());
            return cartDao.save(cart);

        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public void emptyCart() {
        try {
            User user = customerUserDetailsService.getUserDetail();
            Cart cart = cartDao.findByCustomerId(user);
            if (cart == null) {
                return;
            }
            cart.setItems(new ArrayList<>());
            cart.setTotalAmount(0);
            cart.setTotalQuantity(0);
            cartDao.save(cart);
        } catch (Exception ex) {
            throw ex;
        }

    }

    @Override
    public Cart getCartByUserEmail(String emailId) {
        try {
            User user= userService.findByEmail(emailId);
            return cartDao.findByCustomerId(user);
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public Cart getCartForUser(User userDetail) {
        try {
            return cartDao.findByCustomerId(userDetail);
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * Applies quantity changes to the current cart lines and recomputes totals from scratch.
     * Quantity 0 removes a line. New lines are only created for products that exist and are
     * available. (The old loop removed lines from the list it was indexing, which skipped the
     * next line's totals, and it accepted negative quantities and unavailable products.)
     */
    private Cart setCartItemsAndTotalPrice(List<OrderItem> currentCartItems, List<OrderItem> changes) {
        Map<Integer, Integer> requested = new LinkedHashMap<>();
        for (OrderItem change : changes) {
            if (change.getQuantity() < 0) {
                throw new BadRequestException("Quantity can't be negative.");
            }
            requested.put(change.getProductId(), change.getQuantity());
        }

        List<OrderItem> lines = new ArrayList<>();
        for (OrderItem line : currentCartItems) {
            Integer quantity = requested.remove(line.getProductId());
            if (quantity != null) {
                line.setQuantity(quantity);
            }
            if (line.getQuantity() > 0) {
                line.setPrice(line.getPricePerUnit() * line.getQuantity());
                lines.add(line);
            }
        }

        List<Integer> newIds = requested.entrySet().stream()
                .filter(e -> e.getValue() > 0).map(Map.Entry::getKey).toList();
        if (!newIds.isEmpty()) {
            List<ProductWrapper> products = productDao.getProductsWithProductIdIn(newIds);
            for (ProductWrapper product : products) {
                if (!"true".equalsIgnoreCase(product.getStatus())) {
                    throw new BadRequestException(product.getName() + " isn't available right now.");
                }
                OrderItem item = new OrderItem();
                item.setProductId(product.getId());
                item.setProductName(product.getName());
                item.setQuantity(requested.get(product.getId()));
                item.setPricePerUnit(product.getPrice());
                item.setPrice(item.getQuantity() * product.getPrice());
                lines.add(item);
            }
            if (products.size() != newIds.size()) {
                throw new BadRequestException("One of those items is no longer on the menu.");
            }
        }

        Cart cart = new Cart();
        cart.setItems(lines);
        cart.setTotalAmount((float) lines.stream().mapToDouble(OrderItem::getPrice).sum());
        cart.setTotalQuantity(lines.stream().mapToInt(OrderItem::getQuantity).sum());
        return cart;
    }
}
