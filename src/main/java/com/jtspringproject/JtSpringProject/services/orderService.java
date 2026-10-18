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

@Service
public class orderService {

    private final orderDao orderDao;
    private final orderItemDao orderItemDao;
    private final cartProductService cartProductService;

    @Autowired
    public orderService(orderDao orderDao, orderItemDao orderItemDao, cartProductService cartProductService) {
        this.orderDao = orderDao;
        this.orderItemDao = orderItemDao;
        this.cartProductService = cartProductService;
    }

    public Order placeOrder(User customer, String firstName, String lastName, String address,
                             String city, String phoneNumber, String paymentMethod) {

        Cart cart = cartProductService.getOrCreateCart(customer);
        List<CartProduct> cartItems = cartProductService.getCartItems(cart);

        int totalPrice = 0;
        for (CartProduct item : cartItems) {
            totalPrice += item.getProduct().getPrice() * item.getQuantity();
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
        order.setStatus("PLACED");
        order.setOrderDate(new Date());

        orderDao.addOrder(order);

        for (CartProduct item : cartItems) {
            OrderItem orderItem = new OrderItem(order, item.getProduct(), item.getQuantity(), item.getProduct().getPrice());
            orderItemDao.addOrderItem(orderItem);
        }

        // Order place hone ke baad cart khaali kar do
        for (CartProduct item : cartItems) {
            cartProductService.removeProductFromCart(cart, item.getProduct().getId());
        }

        return order;
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