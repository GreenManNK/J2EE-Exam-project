package com.example.hutech.service;

import com.example.hutech.model.Cart;
import com.example.hutech.model.CartItem;
import com.example.hutech.model.Product;
import com.example.hutech.model.User;
import com.example.hutech.repository.CartItemRepository;
import com.example.hutech.repository.CartRepository;
import com.example.hutech.repository.ProductRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public Cart getOrCreateCart(User user) {
        Optional<Cart> existingCart = cartRepository.findByUser(user);
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            if (cart.getItems() == null) {
                cart.setItems(new ArrayList<>());
            }
            return cart;
        }

        Cart newCart = new Cart();
        newCart.setUser(user);
        newCart.setItems(new ArrayList<>());
        return cartRepository.save(newCart);
    }

    public Cart createGuestCart() {
        Cart guestCart = new Cart();
        guestCart.setUser(null);
        guestCart.setItems(new ArrayList<>());
        return cartRepository.save(guestCart);
    }

    public Optional<Cart> getCartById(Long cartId) {
        return cartRepository.findById(cartId);
    }

    public long getTotalItemCount(User user) {
        return cartRepository.findByUser(user)
                .map(cart -> cart.getItems() == null ? 0L : cart.getItems().stream().mapToLong(CartItem::getQuantity).sum())
                .orElse(0L);
    }

    public long getTotalItemCount(Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return 0L;
        }
        return cart.getItems().stream().mapToLong(CartItem::getQuantity).sum();
    }

    public void addToCart(Cart cart, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("So luong phai lon hon 0");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("San pham khong ton tai"));

        if (product.getQuantity() <= 0) {
            throw new IllegalArgumentException("San pham da het hang");
        }

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        int currentInCart = existingItem == null ? 0 : existingItem.getQuantity();
        int nextQuantity = currentInCart + quantity;
        if (nextQuantity > product.getQuantity()) {
            throw new IllegalArgumentException("So luong vuot qua ton kho. Con lai: " + product.getQuantity());
        }

        if (existingItem != null) {
            existingItem.setQuantity(nextQuantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        cartRepository.save(cart);
    }

    public Optional<CartItem> getCartItemById(Long cartItemId) {
        return cartItemRepository.findById(cartItemId);
    }

    public void removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    public void updateCartItemQuantity(Long cartItemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("So luong phai lon hon 0");
        }

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Muc gio hang khong ton tai"));

        int stock = item.getProduct() == null ? 0 : item.getProduct().getQuantity();
        if (quantity > stock) {
            throw new IllegalArgumentException("So luong vuot qua ton kho. Con lai: " + stock);
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    public void clearCart(Cart cart) {
        if (cart.getItems() != null) {
            cart.getItems().clear();
        }
        cartRepository.save(cart);
    }

    public void mergeCarts(Cart sourceCart, Cart targetCart) {
        if (sourceCart == null || targetCart == null || Objects.equals(sourceCart.getId(), targetCart.getId())) {
            return;
        }

        if (sourceCart.getItems() == null || sourceCart.getItems().isEmpty()) {
            return;
        }

        if (targetCart.getItems() == null) {
            targetCart.setItems(new ArrayList<>());
        }

        sourceCart.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getId() == null ? 0L : item.getId()))
                .forEach(sourceItem -> {
                    Product product = sourceItem.getProduct();
                    if (product == null || product.getQuantity() <= 0 || sourceItem.getQuantity() <= 0) {
                        return;
                    }

                    CartItem existingTargetItem = targetCart.getItems().stream()
                            .filter(item -> item.getProduct() != null
                                    && item.getProduct().getId().equals(product.getId()))
                            .findFirst()
                            .orElse(null);

                    int currentTargetQty = existingTargetItem == null ? 0 : existingTargetItem.getQuantity();
                    int mergedQty = Math.min(product.getQuantity(), currentTargetQty + sourceItem.getQuantity());
                    if (mergedQty <= 0) {
                        return;
                    }

                    if (existingTargetItem != null) {
                        existingTargetItem.setQuantity(mergedQty);
                        cartItemRepository.save(existingTargetItem);
                    } else {
                        CartItem newItem = new CartItem();
                        newItem.setCart(targetCart);
                        newItem.setProduct(product);
                        newItem.setQuantity(Math.min(product.getQuantity(), sourceItem.getQuantity()));
                        targetCart.getItems().add(newItem);
                        cartItemRepository.save(newItem);
                    }
                });

        clearCart(sourceCart);
        cartRepository.save(targetCart);
    }
}
