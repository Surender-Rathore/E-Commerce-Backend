package com.jtspringproject.JtSpringProject.dao;

import java.util.List;

import com.jtspringproject.JtSpringProject.models.Order;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class orderDao {
    private final SessionFactory sessionFactory;

    public orderDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public Order addOrder(Order order) {
        this.sessionFactory.getCurrentSession().save(order);
        return order;
    }

    @Transactional
    public List<Order> getOrders() {
        return this.sessionFactory.getCurrentSession()
                .createQuery("from ORDERS order by orderDate desc", Order.class)
                .list();
    }

    @Transactional
    public List<Order> getOrdersByCustomerId(int customerId) {
        return this.sessionFactory.getCurrentSession()
                .createQuery("from ORDERS where customer.id = :customerId order by orderDate desc", Order.class)
                .setParameter("customerId", customerId)
                .list();
    }

    @Transactional
public boolean existsByPaymentIntentId(String paymentIntentId) {
    Long count = this.sessionFactory.getCurrentSession()
            .createQuery("select count(o) from ORDERS o where o.paymentIntentId = :id", Long.class)
            .setParameter("id", paymentIntentId)
            .uniqueResult();
    return count != null && count > 0;
}
}