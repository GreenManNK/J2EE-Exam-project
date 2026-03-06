package com.example.hutech.consoller;

import com.example.hutech.model.Product;
import com.example.hutech.model.Review;
import com.example.hutech.service.ProductService;
import com.example.hutech.service.ReviewService;
import com.example.hutech.service.UserService;
import com.example.hutech.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/products")
public class ProductDetailController {
    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public String viewProductDetail(@PathVariable Long id, Model model, HttpSession session) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product does not exist"));

        List<Review> reviews = reviewService.getReviewsByProduct(product);
        double averageRating = reviewService.getAverageRating(product);
        long totalReviews = reviewService.getTotalReviews(product);

        Long userId = (Long) session.getAttribute("userId");
        boolean isInWishlist = false;
        if (userId != null) {
            var userOpt = userService.getUserById(userId);
            if (userOpt.isPresent()) {
                isInWishlist = wishlistService.isInWishlist(userOpt.get(), product);
            } else {
                session.invalidate();
            }
        }

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("totalReviews", totalReviews);
        model.addAttribute("isInWishlist", isInWishlist);
        model.addAttribute("review", new Review());
        return "/products/product-detail";
    }

    @PostMapping("/{id}/add-review")
    public String addReview(@PathVariable Long id, @ModelAttribute Review review, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        var userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/auth/login";
        }

        var productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/products";
        }

        review.setProduct(productOpt.get());
        review.setUser(userOpt.get());
        reviewService.addReview(review);

        return "redirect:/products/" + id;
    }
}
