package com.example.hutech.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Username là bắt buộc")
    @Column(unique = true)
    private String username;
    
    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Email không hợp lệ")
    @Column(unique = true)
    private String email;
    
    @NotBlank(message = "Mật khẩu là bắt buộc")
    private String password;
    
    private String fullName;
    private String phone;
    private String address;
    
    @Column(columnDefinition = "boolean default true")
    private boolean active = true;
}
