package com.example.hutech.service;

import com.example.hutech.model.Review;
import com.example.hutech.model.Product;
import com.example.hutech.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    
    public List<Review> getReviewsByProduct(Product product) {
        return reviewRepository.findByProduct(product);
    }
    
    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }
    
    public Review addReview(Review review) {
        return reviewRepository.save(review);
    }
    
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new IllegalArgumentException("Review không tồn tại");
        }
        reviewRepository.deleteById(id);
    }
    
    public double getAverageRating(Product product) {
        List<Review> reviews = reviewRepository.findByProduct(product);
        if (reviews.isEmpty()) {
            return 0;
        }
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0);
    }
    
    public long getTotalReviews(Product product) {
        return reviewRepository.findByProduct(product).size();
    }
}
