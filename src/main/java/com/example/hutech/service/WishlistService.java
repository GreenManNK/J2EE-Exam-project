package com.example.hutech.service;

import com.example.hutech.model.Product;
import com.example.hutech.model.User;
import com.example.hutech.model.Wishlist;
import com.example.hutech.repository.WishlistRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {
    private final WishlistRepository wishlistRepository;

    public List<Wishlist> getUserWishlist(User user) {
        return wishlistRepository.findByUser(user);
    }

    public Optional<Wishlist> getWishlistById(Long wishlistId) {
        return wishlistRepository.findById(wishlistId);
    }

    public void addToWishlist(User user, Product product) {
        Optional<Wishlist> existing = wishlistRepository.findByUserAndProduct(user, product);
        if (existing.isEmpty()) {
            Wishlist wishlist = new Wishlist();
            wishlist.setUser(user);
            wishlist.setProduct(product);
            wishlistRepository.save(wishlist);
        }
    }

    public void removeFromWishlist(Long wishlistId) {
        wishlistRepository.deleteById(wishlistId);
    }

    public boolean isInWishlist(User user, Product product) {
        return wishlistRepository.findByUserAndProduct(user, product).isPresent();
    }
}
