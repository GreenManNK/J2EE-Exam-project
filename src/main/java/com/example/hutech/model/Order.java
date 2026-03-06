package com.example.hutech.model;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 private String customerName;
 private String phone;
 private String address;
 private String notes;
 private double totalPrice;
 private String status;
 @OneToMany(mappedBy = "order")
 private List<OrderDetail> orderDetails;
}