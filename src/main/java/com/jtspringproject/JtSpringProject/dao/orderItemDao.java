package com.jtspringproject.JtSpringProject.dao;

import java.util.List;

import com.jtspringproject.JtSpringProject.models.OrderItem;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class orderItemDao {
    private final SessionFactory sessionFactory;

    public orderItemDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public OrderItem addOrderItem(OrderItem orderItem) {
        this.sessionFactory.getCurrentSession().save(orderItem);
        return orderItem;
    }

    @Transactional
    public List<OrderItem> getItemsByOrderId(int orderId) {
        return this.sessionFactory.getCurrentSession()
                .createQuery("from OrderItem where order.id = :orderId", OrderItem.class)
                .setParameter("orderId", orderId)
                .list();
    }
}