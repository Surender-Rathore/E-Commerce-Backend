package com.jtspringproject.JtSpringProject.services;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jtspringproject.JtSpringProject.dao.orderDao;
import com.jtspringproject.JtSpringProject.dao.orderItemDao;
import com.jtspringproject.JtSpringProject.models.Cart;
import com.jtspringproject.JtSpringProject.models.CartProduct;
import com.jtspringproject.JtSpringProject.models.Order;
import com.jtspringproject.JtSpringProject.models.OrderItem;
import com.jtspringproject.JtSpringProject.models.User;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

@Service
public class orderService {

    public static final int SHIPPING_FEE = 200;

    private final orderDao orderDao;
    private final orderItemDao orderItemDao;
    private final cartProductService cartProductService;

    @Autowired
    public orderService(orderDao orderDao, orderItemDao orderItemDao, cartProductService cartProductService) {
        this.orderDao = orderDao;
        this.orderItemDao = orderItemDao;
        this.cartProductService = cartProductService;
    }

    private int itemsTotal(List<CartProduct> cartItems) {
        int total = 0;
        for (CartProduct item : cartItems) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        return total;
    }

    // Cart ka total (shipping ke sath). Cart khaali ho to 0.
    public int calculateTotal(User customer) {
        Cart cart = cartProductService.getOrCreateCart(customer);
        List<CartProduct> cartItems = cartProductService.getCartItems(cart);
        if (cartItems.isEmpty()) {
            return 0;
        }
        return itemsTotal(cartItems) + SHIPPING_FEE;
    }

    public Order placeOrder(User customer, String firstName, String lastName, String address,
                             String city, String phoneNumber, String paymentMethod, String paymentIntentId) {

        Cart cart = cartProductService.getOrCreateCart(customer);
        List<CartProduct> cartItems = cartProductService.getCartItems(cart);

        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        int totalPrice = itemsTotal(cartItems) + SHIPPING_FEE;

        String status;
        String intentToSave = null;

        if ("card".equals(paymentMethod)) {
            verifyCardPayment(paymentIntentId, totalPrice);
            status = "PAID";
            intentToSave = paymentIntentId;
        } else {
            status = "PLACED"; // Cash on delivery
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setFirstName(firstName);
        order.setLastName(lastName);
        order.setAddress(address);
        order.setCity(city);
        order.setPhoneNumber(phoneNumber);
        order.setPaymentMethod(paymentMethod);
        order.setTotalPrice(totalPrice);
        order.setStatus(status);
        order.setOrderDate(new Date());
        order.setPaymentIntentId(intentToSave);

        orderDao.addOrder(order);

        for (CartProduct item : cartItems) {
            OrderItem orderItem = new OrderItem(order, item.getProduct(), item.getQuantity(), item.getProduct().getPrice());
            orderItemDao.addOrderItem(orderItem);
        }

        for (CartProduct item : cartItems) {
            cartProductService.removeProductFromCart(cart, item.getProduct().getId());
        }

        return order;
    }

    private void verifyCardPayment(String paymentIntentId, int totalPrice) {
        if (paymentIntentId == null || paymentIntentId.isEmpty()) {
            throw new IllegalStateException("Payment not found");
        }
        if (orderDao.existsByPaymentIntentId(paymentIntentId)) {
            throw new IllegalStateException("This payment was already used");
        }
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            if (!"succeeded".equals(intent.getStatus())) {
                throw new IllegalStateException("Payment not completed");
            }
            if (intent.getAmount() == null || intent.getAmount() != totalPrice * 100L) {
                throw new IllegalStateException("Payment amount mismatch");
            }
        } catch (StripeException e) {
            throw new IllegalStateException("Could not verify payment");
        }
    }

    public List<Order> getAllOrders() {
        return orderDao.getOrders();
    }

    public List<Order> getOrdersByCustomer(int customerId) {
        return orderDao.getOrdersByCustomerId(customerId);
    }

    public List<OrderItem> getOrderItems(int orderId) {
        return orderItemDao.getItemsByOrderId(orderId);
    }
}