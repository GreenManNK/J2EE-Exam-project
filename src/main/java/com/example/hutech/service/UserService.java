package com.example.hutech.service;

import com.example.hutech.model.User;
import com.example.hutech.repository.UserRepository;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    public static final String ADMIN_USERNAME = "admin";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByLoginIdentifier(String identifier) {
        String normalizedIdentifier = identifier == null ? "" : identifier.trim();
        if (normalizedIdentifier.isBlank()) {
            return Optional.empty();
        }

        Optional<User> byUsername = userRepository.findByUsername(normalizedIdentifier);
        if (byUsername.isPresent()) {
            return byUsername;
        }

        Optional<User> byEmail = userRepository.findByEmail(normalizedIdentifier.toLowerCase(Locale.ROOT));
        if (byEmail.isPresent()) {
            return byEmail;
        }

        String normalizedPhone;
        try {
            normalizedPhone = normalizePhone(normalizedIdentifier);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        if (normalizedPhone == null) {
            return Optional.empty();
        }
        return userRepository.findByPhone(normalizedPhone);
    }

    public User registerUser(User user) {
        String username = user.getUsername() == null ? "" : user.getUsername().trim();
        String email = user.getEmail() == null ? "" : user.getEmail().trim().toLowerCase(Locale.ROOT);
        String password = user.getPassword() == null ? "" : user.getPassword();
        String phone = normalizePhone(user.getPhone());

        if (username.isBlank()) {
            throw new IllegalArgumentException("Username la bat buoc");
        }
        if (ADMIN_USERNAME.equalsIgnoreCase(username)) {
            throw new IllegalArgumentException("Username admin da duoc dat truoc");
        }
        if (email.isBlank()) {
            throw new IllegalArgumentException("Email la bat buoc");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Mat khau phai co it nhat 6 ky tu");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username da ton tai");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email da ton tai");
        }
        if (phone != null && userRepository.findByPhone(phone).isPresent()) {
            throw new IllegalArgumentException("So dien thoai da ton tai");
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setFacebookId(null);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public User getOrCreateFacebookUser(String facebookId, String fullName, String email) {
        String normalizedFacebookId = facebookId == null ? "" : facebookId.trim();
        if (normalizedFacebookId.isBlank()) {
            throw new IllegalArgumentException("Thong tin Facebook khong hop le");
        }

        Optional<User> existingFacebookUser = userRepository.findByFacebookId(normalizedFacebookId);
        if (existingFacebookUser.isPresent()) {
            User user = existingFacebookUser.get();
            if (!user.isActive()) {
                throw new IllegalArgumentException("Tai khoan da bi khoa");
            }
            return user;
        }

        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (normalizedEmail.isBlank()) {
            normalizedEmail = "facebook_" + normalizedFacebookId + "@facebook.local";
        }

        Optional<User> existingByEmail = userRepository.findByEmail(normalizedEmail);
        if (existingByEmail.isPresent()) {
            User user = existingByEmail.get();
            if (!user.isActive()) {
                throw new IllegalArgumentException("Tai khoan da bi khoa");
            }
            user.setFacebookId(normalizedFacebookId);
            if ((user.getFullName() == null || user.getFullName().isBlank()) && fullName != null && !fullName.isBlank()) {
                user.setFullName(fullName.trim());
            }
            return userRepository.save(user);
        }

        User user = new User();
        user.setUsername(generateUniqueUsername("fb_" + normalizedFacebookId));
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setFullName(fullName == null ? null : fullName.trim());
        user.setPhone(null);
        user.setAddress(null);
        user.setFacebookId(normalizedFacebookId);
        user.setActive(true);
        return userRepository.save(user);
    }

    public boolean isAdmin(User user) {
        if (user == null || user.getUsername() == null) {
            return false;
        }
        return ADMIN_USERNAME.equalsIgnoreCase(user.getUsername().trim());
    }

    public boolean verifyPassword(User user, String rawPassword) {
        if (user == null || rawPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public void updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("User khong ton tai"));
        String normalizedEmail = user.getEmail() == null ? "" : user.getEmail().trim().toLowerCase(Locale.ROOT);
        String normalizedPhone = normalizePhone(user.getPhone());

        if (normalizedEmail.isBlank()) {
            throw new IllegalStateException("Email la bat buoc");
        }

        userRepository.findByEmail(normalizedEmail)
                .filter(found -> !found.getId().equals(existingUser.getId()))
                .ifPresent(found -> {
                    throw new IllegalStateException("Email da ton tai");
                });

        if (normalizedPhone != null) {
            userRepository.findByPhone(normalizedPhone)
                    .filter(found -> !found.getId().equals(existingUser.getId()))
                    .ifPresent(found -> {
                        throw new IllegalStateException("So dien thoai da ton tai");
                    });
        }

        existingUser.setFullName(user.getFullName());
        existingUser.setPhone(normalizedPhone);
        existingUser.setAddress(user.getAddress());
        existingUser.setEmail(normalizedEmail);
        userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalStateException("User khong ton tai");
        }
        userRepository.deleteById(id);
    }

    public String normalizePhone(String rawPhone) {
        if (rawPhone == null) {
            return null;
        }

        String normalized = rawPhone.replaceAll("[\\s().-]", "").trim();
        if (normalized.isBlank()) {
            return null;
        }

        if (normalized.startsWith("+84")) {
            normalized = "0" + normalized.substring(3);
        } else if (normalized.startsWith("84") && normalized.length() >= 10) {
            normalized = "0" + normalized.substring(2);
        }

        if (!normalized.matches("0\\d{8,10}")) {
            throw new IllegalArgumentException("So dien thoai khong hop le");
        }

        return normalized;
    }

    private String generateUniqueUsername(String baseUsername) {
        String candidate = baseUsername;
        int index = 1;
        while (userRepository.findByUsername(candidate).isPresent() || ADMIN_USERNAME.equalsIgnoreCase(candidate)) {
            candidate = baseUsername + "_" + index;
            index++;
        }
        return candidate;
    }
}
