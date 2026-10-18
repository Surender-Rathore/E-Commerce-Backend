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

import com.jtspringproject.JtSpringProject.models.Cart;
import com.jtspringproject.JtSpringProject.models.CartProduct;
import com.jtspringproject.JtSpringProject.models.User;
import com.jtspringproject.JtSpringProject.services.cartProductService;
import com.jtspringproject.JtSpringProject.services.userService;

@RestController
public class CartController {

    private final cartProductService cartProductService;
    private final userService userService;

    @Autowired
    public CartController(cartProductService cartProductService, userService userService) {
        this.cartProductService = cartProductService;
        this.userService = userService;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username);
    }

    @GetMapping("/api/cart")
    @ResponseBody
    public ResponseEntity<List<CartProduct>> getCart() {
        User user = getCurrentUser();
        Cart cart = cartProductService.getOrCreateCart(user);
        List<CartProduct> items = cartProductService.getCartItems(cart);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/api/cart/add")
    @ResponseBody
    public ResponseEntity<String> addToCart(@RequestParam("productId") int productId,
                                             @RequestParam(value = "quantity", defaultValue = "1") int quantity) {
        User user = getCurrentUser();
        Cart cart = cartProductService.getOrCreateCart(user);
        cartProductService.addProductToCart(cart, productId, quantity);
        return ResponseEntity.ok("Added to cart");
    }

    @PostMapping("/api/cart/update")
    @ResponseBody
    public ResponseEntity<String> updateCartItem(@RequestParam("productId") int productId,
                                                  @RequestParam("quantity") int quantity) {
        User user = getCurrentUser();
        Cart cart = cartProductService.getOrCreateCart(user);
        cartProductService.updateQuantity(cart, productId, quantity);
        return ResponseEntity.ok("Updated");
    }

    @PostMapping("/api/cart/remove")
    @ResponseBody
    public ResponseEntity<String> removeCartItem(@RequestParam("productId") int productId) {
        User user = getCurrentUser();
        Cart cart = cartProductService.getOrCreateCart(user);
        cartProductService.removeProductFromCart(cart, productId);
        return ResponseEntity.ok("Removed");
    }
}