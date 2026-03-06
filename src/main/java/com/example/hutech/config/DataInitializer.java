package com.example.hutech.config;

import com.example.hutech.model.Category;
import com.example.hutech.model.Product;
import com.example.hutech.model.User;
import com.example.hutech.repository.CategoryRepository;
import com.example.hutech.repository.ProductRepository;
import com.example.hutech.repository.UserRepository;
import com.example.hutech.service.UserService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Bean
    CommandLineRunner seedAdminUser() {
        return args -> {
            if (userRepository.findByUsername(UserService.ADMIN_USERNAME).isPresent()) {
                return;
            }

            User admin = new User();
            admin.setUsername(UserService.ADMIN_USERNAME);
            admin.setEmail("admin@tgdd.local");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setFullName("System Admin");
            admin.setActive(true);
            userRepository.save(admin);
        };
    }

    @Bean
    CommandLineRunner seedCatalogData() {
        return args -> {
            if (categoryRepository.count() > 0 || productRepository.count() > 0) {
                return;
            }

            Category phone = categoryRepository.save(new Category(null, "Dien thoai", "fas fa-mobile-screen-button"));
            Category laptop = categoryRepository.save(new Category(null, "Laptop", "fas fa-laptop"));
            Category tablet = categoryRepository.save(new Category(null, "Tablet", "fas fa-tablet-screen-button"));
            Category watch = categoryRepository.save(new Category(null, "Dong ho", "fas fa-clock"));
            Category accessory = categoryRepository.save(new Category(null, "Phu kien", "fas fa-headphones"));
            Category smartHome = categoryRepository.save(new Category(null, "Nha thong minh", "fas fa-house-signal"));

            productRepository.saveAll(List.of(
                    new Product(null, "iPhone 15 128GB", 19990000, "Ban tieu chuan, camera 48MP.", "https://placehold.co/600x600/f5f5f5/111111?text=iPhone+15+128GB", 25, 10, true, phone),
                    new Product(null, "Samsung Galaxy S24 256GB", 18990000, "Man hinh 120Hz, AI camera.", "https://placehold.co/600x600/f3f4f6/111111?text=Samsung+Galaxy+S24+256GB", 18, 12, true, phone),
                    new Product(null, "Xiaomi 14 256GB", 14990000, "Snapdragon 8 Gen 3.", "https://placehold.co/600x600/f9fafb/111111?text=Xiaomi+14+256GB", 32, 5, false, phone),
                    new Product(null, "MacBook Air M3 13 inch", 26990000, "Nhe, pin dai, phu hop van phong.", "https://placehold.co/600x600/f5f5f5/111111?text=MacBook+Air+M3+13", 14, 8, false, laptop),
                    new Product(null, "Acer Nitro V 15", 21990000, "Laptop gaming RTX 4050.", "https://placehold.co/600x600/f3f4f6/111111?text=Acer+Nitro+V+15", 9, 15, true, laptop),
                    new Product(null, "iPad Air M2 11 inch", 16990000, "Tablet cho hoc tap va cong viec.", "https://placehold.co/600x600/f9fafb/111111?text=iPad+Air+M2+11", 20, 7, false, tablet),
                    new Product(null, "Galaxy Tab S9 FE", 10990000, "But S-Pen di kem.", "https://placehold.co/600x600/f5f5f5/111111?text=Galaxy+Tab+S9+FE", 16, 10, false, tablet),
                    new Product(null, "Apple Watch Series 9", 9990000, "Theo doi suc khoe va thong bao.", "https://placehold.co/600x600/f3f4f6/111111?text=Apple+Watch+Series+9", 22, 18, true, watch),
                    new Product(null, "Garmin Forerunner 255", 6990000, "Dong ho chay bo GPS chuyen sau.", "https://placehold.co/600x600/f9fafb/111111?text=Garmin+Forerunner+255", 8, 0, false, watch),
                    new Product(null, "AirPods Pro 2 USB-C", 5690000, "Chong on chu dong ANC.", "https://placehold.co/600x600/f5f5f5/111111?text=AirPods+Pro+2+USB-C", 30, 20, true, accessory),
                    new Product(null, "Sac du phong 20000mAh", 790000, "Cong suat 22.5W, 2 cong USB.", "https://placehold.co/600x600/f3f4f6/111111?text=Sac+du+phong+20000mAh", 0, 25, false, accessory),
                    new Product(null, "Loa JBL Flip 6", 2490000, "Am thanh manh, chong nuoc IP67.", "https://placehold.co/600x600/f9fafb/111111?text=Loa+JBL+Flip+6", 11, 10, true, accessory),
                    new Product(null, "Robot hut bui Xiaomi S10", 5490000, "Hut va lau thong minh.", "https://placehold.co/600x600/f5f5f5/111111?text=Robot+hut+bui+Xiaomi+S10", 7, 12, false, smartHome)));
        };
    }

    @Bean
    CommandLineRunner seedMissingProductImages() {
        return args -> {
            Map<String, String> imageMap = Map.ofEntries(
                    Map.entry("iPhone 15 128GB", "https://placehold.co/600x600/f5f5f5/111111?text=iPhone+15+128GB"),
                    Map.entry("Samsung Galaxy S24 256GB", "https://placehold.co/600x600/f3f4f6/111111?text=Samsung+Galaxy+S24+256GB"),
                    Map.entry("Xiaomi 14 256GB", "https://placehold.co/600x600/f9fafb/111111?text=Xiaomi+14+256GB"),
                    Map.entry("MacBook Air M3 13 inch", "https://placehold.co/600x600/f5f5f5/111111?text=MacBook+Air+M3+13"),
                    Map.entry("Acer Nitro V 15", "https://placehold.co/600x600/f3f4f6/111111?text=Acer+Nitro+V+15"),
                    Map.entry("iPad Air M2 11 inch", "https://placehold.co/600x600/f9fafb/111111?text=iPad+Air+M2+11"),
                    Map.entry("Galaxy Tab S9 FE", "https://placehold.co/600x600/f5f5f5/111111?text=Galaxy+Tab+S9+FE"),
                    Map.entry("Apple Watch Series 9", "https://placehold.co/600x600/f3f4f6/111111?text=Apple+Watch+Series+9"),
                    Map.entry("Garmin Forerunner 255", "https://placehold.co/600x600/f9fafb/111111?text=Garmin+Forerunner+255"),
                    Map.entry("AirPods Pro 2 USB-C", "https://placehold.co/600x600/f5f5f5/111111?text=AirPods+Pro+2+USB-C"),
                    Map.entry("Sac du phong 20000mAh", "https://placehold.co/600x600/f3f4f6/111111?text=Sac+du+phong+20000mAh"),
                    Map.entry("Loa JBL Flip 6", "https://placehold.co/600x600/f9fafb/111111?text=Loa+JBL+Flip+6"),
                    Map.entry("Robot hut bui Xiaomi S10", "https://placehold.co/600x600/f5f5f5/111111?text=Robot+hut+bui+Xiaomi+S10"));

            var products = productRepository.findAll();
            boolean changed = false;
            for (Product product : products) {
                if (product.getImageUrl() != null && !product.getImageUrl().isBlank()) {
                    continue;
                }
                String imageUrl = imageMap.get(product.getName());
                if (imageUrl == null) {
                    continue;
                }
                product.setImageUrl(imageUrl);
                changed = true;
            }

            if (changed) {
                productRepository.saveAll(products);
            }
        };
    }
}
