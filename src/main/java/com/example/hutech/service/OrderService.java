package com.example.hutech.service;

import com.example.hutech.model.Cart;
import com.example.hutech.model.CartItem;
import com.example.hutech.model.Order;
import com.example.hutech.model.OrderDetail;
import com.example.hutech.model.Product;
import com.example.hutech.model.User;
import com.example.hutech.repository.OrderRepository;
import com.example.hutech.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;

/**
* Service class for managing orders.
*/
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    
    /**
     * Retrieve all orders from the database.
     * @return a list of orders
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByIdDesc();
    }

    public List<Order> getOrdersByUser(User user) {
        if (user == null) {
            return List.of();
        }
        return orderRepository.findByUserOrderByIdDesc(user);
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

    public Order createOrderFromCart(User user, Cart cart) {
        if (user == null) {
            throw new IllegalArgumentException("Vui long dang nhap de thanh toan.");
        }
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Gio hang dang trong.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setCustomerName(user.getFullName() == null || user.getFullName().isBlank()
                ? user.getUsername()
                : user.getFullName());
        order.setPhone(user.getPhone() == null ? "" : user.getPhone());
        order.setAddress(user.getAddress() == null ? "" : user.getAddress());
        order.setNotes("Dat tu gio hang");
        order.setStatus("pending");

        List<OrderDetail> details = new ArrayList<>();
        double totalPrice = 0D;

        for (CartItem cartItem : cart.getItems()) {
            if (cartItem == null || cartItem.getQuantity() <= 0 || cartItem.getProduct() == null) {
                continue;
            }

            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("San pham khong ton tai."));

            int requestedQuantity = cartItem.getQuantity();
            if (product.getQuantity() < requestedQuantity) {
                throw new IllegalArgumentException("San pham " + product.getName()
                        + " khong du ton kho. Con lai: " + product.getQuantity());
            }

            double unitPrice = product.getPrice();
            double lineTotal = unitPrice * requestedQuantity;

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setProductName(product.getName());
            detail.setQuantity(requestedQuantity);
            detail.setUnitPrice(unitPrice);
            detail.setLineTotal(lineTotal);
            details.add(detail);

            product.setQuantity(product.getQuantity() - requestedQuantity);
            productRepository.save(product);
            totalPrice += lineTotal;
        }

        if (details.isEmpty()) {
            throw new IllegalArgumentException("Gio hang dang trong.");
        }

        order.setTotalPrice(totalPrice);
        order.setOrderDetails(details);
        return orderRepository.save(order);
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
