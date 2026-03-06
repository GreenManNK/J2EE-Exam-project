package com.example.hutech.repository;

import com.example.hutech.model.Order;
import com.example.hutech.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByOrderByIdDesc();

    List<Order> findByUserOrderByIdDesc(User user);
}
