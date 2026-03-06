package com.example.hutech.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
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

 @ManyToOne
 @JoinColumn(name = "user_id")
 private User user;

 @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
 private List<OrderDetail> orderDetails;

 private LocalDateTime createdAt;

 @PrePersist
 protected void onCreate() {
  createdAt = LocalDateTime.now();
 }
}
