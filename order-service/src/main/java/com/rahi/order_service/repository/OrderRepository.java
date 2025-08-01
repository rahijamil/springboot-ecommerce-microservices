package com.rahi.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rahi.order_service.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

}
