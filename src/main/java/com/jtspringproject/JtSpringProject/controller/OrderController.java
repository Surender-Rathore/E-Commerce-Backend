package com.jtspringproject.JtSpringProject.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jtspringproject.JtSpringProject.models.Order;
import com.jtspringproject.JtSpringProject.models.User;
import com.jtspringproject.JtSpringProject.services.orderService;
import com.jtspringproject.JtSpringProject.services.userService;

@RestController
public class OrderController {

    private final orderService orderService;
    private final userService userService;

    @Autowired
    public OrderController(orderService orderService, userService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username);
    }

    @PostMapping("/api/orders/place")
    @ResponseBody
    public ResponseEntity<Order> placeOrder(@RequestParam("firstName") String firstName,
                                             @RequestParam("lastName") String lastName,
                                             @RequestParam("address") String address,
                                             @RequestParam("city") String city,
                                             @RequestParam("phoneNumber") String phoneNumber,
                                             @RequestParam("paymentMethod") String paymentMethod) {
        User customer = getCurrentUser();
        Order order = orderService.placeOrder(customer, firstName, lastName, address, city, phoneNumber, paymentMethod);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/api/orders")
    @ResponseBody
    public ResponseEntity<List<Order>> getMyOrders() {
        User customer = getCurrentUser();
        List<Order> orders = orderService.getOrdersByCustomer(customer.getId());
        return ResponseEntity.ok(orders);
    }
}