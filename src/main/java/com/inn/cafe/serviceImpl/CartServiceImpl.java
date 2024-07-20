package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.POJO.Cart;
import com.inn.cafe.POJO.User;
import com.inn.cafe.dao.CartDao;
import com.inn.cafe.dao.ProductDao;
import com.inn.cafe.dto.OrderItem;
import com.inn.cafe.service.CartService;
import com.inn.cafe.wrapper.ProductWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartDao cartDao;

    @Autowired
    ProductDao productDao;

    @Autowired
    CustomerUserDetailsService customerUserDetailsService;

    @Override
    public Cart addOrUpdateItem(List<OrderItem> items) {
        try {

            User user = customerUserDetailsService.getUserDetail();
            Cart cart = cartDao.findByCustomerId(user);

            Cart updatedCart;
            if (Objects.isNull(cart)) {
                updatedCart = setCartItemsAndTotalPrice(new ArrayList<>(), items);
            } else {
                updatedCart = setCartItemsAndTotalPrice(cart.getItems(), items);
                updatedCart.setId(cart.getId());
            }

            updatedCart.setCustomerId(user);
            return cartDao.save(updatedCart);

        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public void emptyCart() {
        try {
            User user = customerUserDetailsService.getUserDetail();
            Cart cart = cartDao.findByCustomerId(user);
            cart.setItems(new ArrayList<>());
            cart.setTotalAmount(0);
            cartDao.save(cart);
        } catch (Exception ex) {
            throw ex;
        }

    }

    private Cart setCartItemsAndTotalPrice(List<OrderItem> currentCartItems, List<OrderItem> itemsToAdd) {

        HashMap<Integer, Integer> productIdToQuantityMap = new HashMap<>();
        HashMap<Integer, Boolean> productAlreadyInCart = new HashMap<>();
        List<Integer> productIds = new ArrayList<>();
        for (OrderItem item : itemsToAdd) {
            productIds.add(item.getProductId());
            productIdToQuantityMap.put(item.getProductId(), item.getQuantity());
        }

        List<ProductWrapper> products = productDao.getProductsWithProductIdIn(productIds);

        HashMap<Integer, OrderItem> productIdToOrderItemMapForNewItems = new HashMap<>();
        for (OrderItem item : itemsToAdd) {
            productIdToOrderItemMapForNewItems.put(item.getProductId(), item);
        }


        float totalPrice = 0;
        int totalQuantity = 0;

        int currSize = currentCartItems.size();
        for (int i = 0; i < currSize; i++) {
            OrderItem item = currentCartItems.get(i);
            if (productIdToOrderItemMapForNewItems.containsKey(item.getProductId())) {
                productAlreadyInCart.put(item.getProductId(), Boolean.TRUE);
                OrderItem newOrderItem = productIdToOrderItemMapForNewItems.get(item.getProductId());
                item.setQuantity(newOrderItem.getQuantity());
                item.setPrice(item.getPricePerUnit() * item.getQuantity());

                if (item.getQuantity() == 0) {
                    currentCartItems.remove(item);
                    currSize--;
                } else {
                    totalPrice += item.getPrice();
                    totalQuantity += item.getQuantity();
                }
            } else {
                totalPrice += item.getPrice();
                totalQuantity += item.getQuantity();
            }
        }

        for (ProductWrapper product : products) {
            if (!productAlreadyInCart.getOrDefault(product.getId(), Boolean.FALSE)) {
                OrderItem item = new OrderItem();
                item.setProductId(product.getId());
                item.setProductName(product.getName());
                item.setQuantity(productIdToQuantityMap.get(product.getId()));
                item.setPricePerUnit(product.getPrice());
                item.setPrice(productIdToQuantityMap.get(product.getId()) * product.getPrice());

                currentCartItems.add(item);
                totalPrice += item.getPrice();
                totalQuantity += item.getQuantity();
            }
        }

        Cart cart = new Cart();
        cart.setItems(currentCartItems);
        cart.setTotalAmount(totalPrice);
        cart.setTotalQuantity(totalQuantity);

        return cart;

    }
}
