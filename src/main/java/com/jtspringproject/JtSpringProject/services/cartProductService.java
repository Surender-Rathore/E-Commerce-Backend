package com.jtspringproject.JtSpringProject.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jtspringproject.JtSpringProject.dao.cartDao;
import com.jtspringproject.JtSpringProject.dao.cartProductDao;
import com.jtspringproject.JtSpringProject.models.Cart;
import com.jtspringproject.JtSpringProject.models.CartProduct;
import com.jtspringproject.JtSpringProject.models.Product;
import com.jtspringproject.JtSpringProject.models.User;

@Service
public class cartProductService {

    private final cartProductDao cartProductDao;
    private final cartDao cartDao;
    private final productService productService;

    @Autowired
    public cartProductService(cartProductDao cartProductDao, cartDao cartDao, productService productService) {
        this.cartProductDao = cartProductDao;
        this.cartDao = cartDao;
        this.productService = productService;
    }

    public Cart getOrCreateCart(User customer) {
        Cart cart = cartDao.getCartByCustomerId(customer.getId());
        if (cart == null) {
            Cart newCart = new Cart();
            newCart.setCustomer(customer);
            cart = cartDao.addCart(newCart);
        }
        return cart;
    }

    public List<CartProduct> getCartItems(Cart cart) {
        return cartProductDao.getCartProductsByCartId(cart.getId());
    }

    public CartProduct addProductToCart(Cart cart, int productId, int quantity) {
        CartProduct existing = cartProductDao.getCartProduct(cart.getId(), productId);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            cartProductDao.updateCartProduct(existing);
            return existing;
        } else {
            Product product = productService.getProduct(productId);
            CartProduct newItem = new CartProduct(cart, product, quantity);
            return cartProductDao.addCartProduct(newItem);
        }
    }

    public void updateQuantity(Cart cart, int productId, int quantity) {
        CartProduct existing = cartProductDao.getCartProduct(cart.getId(), productId);
        if (existing != null) {
            existing.setQuantity(quantity);
            cartProductDao.updateCartProduct(existing);
        }
    }

    public void removeProductFromCart(Cart cart, int productId) {
        CartProduct existing = cartProductDao.getCartProduct(cart.getId(), productId);
        if (existing != null) {
            cartProductDao.deleteCartProduct(existing);
        }
    }
}