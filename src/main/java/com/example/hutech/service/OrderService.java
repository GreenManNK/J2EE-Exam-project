package com.example.hutech.service;

import com.example.hutech.model.Order;
import com.example.hutech.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
* Service class for managing orders.
*/
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    
    /**
     * Retrieve all orders from the database.
     * @return a list of orders
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    /**
     * Retrieve an order by its id.
     * @param id the id of the order to retrieve
     * @return an Optional containing the found order or empty if not found
     */
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
    
    /**
     * Add or update an order in the database.
     * @param order the order to save
     */
    public void saveOrder(Order order) {
        orderRepository.save(order);
    }
    
    /**
     * Update an existing order.
     * @param order the order with updated information
     */
    public void updateOrder(@NotNull Order order) {
        Order existingOrder = orderRepository.findById(order.getId())
                .orElseThrow(() -> new IllegalStateException("Order with ID " + 
                        order.getId() + " does not exist."));
        existingOrder.setCustomerName(order.getCustomerName());
        existingOrder.setPhone(order.getPhone());
        existingOrder.setAddress(order.getAddress());
        existingOrder.setNotes(order.getNotes());
        existingOrder.setTotalPrice(order.getTotalPrice());
        existingOrder.setStatus(order.getStatus());
        existingOrder.setOrderDetails(order.getOrderDetails());
        orderRepository.save(existingOrder);
    }
    
    /**
     * Delete an order by its id.
     * @param id the id of the order to delete
     */
    public void deleteOrderById(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new IllegalStateException("Order with ID " + id + " does not exist.");
        }
        orderRepository.deleteById(id);
    }
    
    public void deleteOrder(Long id) {
        deleteOrderById(id);
    }
}
