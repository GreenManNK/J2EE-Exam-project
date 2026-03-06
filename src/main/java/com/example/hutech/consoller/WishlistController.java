package com.example.hutech.consoller;

import com.example.hutech.model.Product;
import com.example.hutech.model.User;
import com.example.hutech.service.ProductService;
import com.example.hutech.service.UserService;
import com.example.hutech.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {
    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public String viewWishlist(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }

        var wishlist = wishlistService.getUserWishlist(user);
        model.addAttribute("wishlist", wishlist);
        return "/wishlist/view-wishlist";
    }

    @PostMapping("/add/{productId}")
    public String addToWishlist(@PathVariable Long productId, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }

        var productOpt = productService.getProductById(productId);
        if (productOpt.isEmpty()) {
            return "redirect:/products";
        }

        Product product = productOpt.get();
        wishlistService.addToWishlist(user, product);
        return "redirect:/products/" + productId;
    }

    @PostMapping("/remove/{wishlistId}")
    public String removeFromWishlist(@PathVariable Long wishlistId, HttpSession session) {
        User user = getCurrentUser(session);
        if (user == null) {
            return "redirect:/auth/login";
        }

        boolean itemBelongsToCurrentUser = wishlistService.getWishlistById(wishlistId)
                .map(item -> item.getUser() != null && item.getUser().getId().equals(user.getId()))
                .orElse(false);

        if (itemBelongsToCurrentUser) {
            wishlistService.removeFromWishlist(wishlistId);
        }

        return "redirect:/wishlist";
    }

    private User getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return null;
        }

        var userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return null;
        }

        return userOpt.get();
    }
}
