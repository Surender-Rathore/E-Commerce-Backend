package com.jtspringproject.JtSpringProject.dao;

import java.util.List;

import com.jtspringproject.JtSpringProject.models.CartProduct;
import com.jtspringproject.JtSpringProject.models.CartProductId;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class cartProductDao {
    private final SessionFactory sessionFactory;

    public cartProductDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public CartProduct addCartProduct(CartProduct cartProduct) {
        this.sessionFactory.getCurrentSession().save(cartProduct);
        return cartProduct;
    }

    @Transactional
    public List<CartProduct> getCartProductsByCartId(int cartId) {
        return this.sessionFactory.getCurrentSession()
                .createQuery("from CartProduct where cart.id = :cartId", CartProduct.class)
                .setParameter("cartId", cartId)
                .list();
    }

    @Transactional
    public CartProduct getCartProduct(int cartId, int productId) {
        CartProductId id = new CartProductId(cartId, productId);
        return this.sessionFactory.getCurrentSession().get(CartProduct.class, id);
    }

    @Transactional
    public void updateCartProduct(CartProduct cartProduct) {
        this.sessionFactory.getCurrentSession().update(cartProduct);
    }

    @Transactional
    public void deleteCartProduct(CartProduct cartProduct) {
        this.sessionFactory.getCurrentSession().delete(cartProduct);
    }
}