package com.jtspringproject.JtSpringProject.dao;

import java.util.List;

import com.jtspringproject.JtSpringProject.models.Cart;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class cartDao {
    private final SessionFactory sessionFactory;

    public cartDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public Cart addCart(Cart cart) {
        this.sessionFactory.getCurrentSession().save(cart);
        return cart;
    }

    @Transactional
    public List<Cart> getCarts() {
        return this.sessionFactory.getCurrentSession().createQuery("from CART", Cart.class).list();
    }

    @Transactional
    public Cart getCartByCustomerId(int customerId) {
        List<Cart> carts = this.sessionFactory.getCurrentSession()
                .createQuery("from CART where customer.id = :customerId", Cart.class)
                .setParameter("customerId", customerId)
                .list();
        return carts.isEmpty() ? null : carts.get(0);
    }

    @Transactional
    public void updateCart(Cart cart) {
        this.sessionFactory.getCurrentSession().update(cart);
    }

    @Transactional
    public void deleteCart(Cart cart) {
        this.sessionFactory.getCurrentSession().delete(cart);
    }
}