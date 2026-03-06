package com.example.hutech.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_details")
public class OrderDetail {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 private String productName;
 private int quantity;
 private double unitPrice;
 private double lineTotal;

 @ManyToOne
 @JoinColumn(name = "product_id")
 private Product product;

 @ManyToOne
 @JoinColumn(name = "order_id")
 private Order order;
}
