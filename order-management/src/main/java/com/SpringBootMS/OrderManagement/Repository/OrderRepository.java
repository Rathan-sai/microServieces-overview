package com.SpringBootMS.OrderManagement.Repository;

import com.SpringBootMS.OrderManagement.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
