package com.example.hutech.config;

import com.example.hutech.model.Category;
import com.example.hutech.model.Product;
import com.example.hutech.repository.CategoryRepository;
import com.example.hutech.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

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
                    new Product(null, "iPhone 15 128GB", 19990000, "Ban tieu chuan, camera 48MP.", 25, 10, true, phone),
                    new Product(null, "Samsung Galaxy S24 256GB", 18990000, "Man hinh 120Hz, AI camera.", 18, 12, true, phone),
                    new Product(null, "Xiaomi 14 256GB", 14990000, "Snapdragon 8 Gen 3.", 32, 5, false, phone),
                    new Product(null, "MacBook Air M3 13 inch", 26990000, "Nhe, pin dai, phu hop van phong.", 14, 8, false, laptop),
                    new Product(null, "Acer Nitro V 15", 21990000, "Laptop gaming RTX 4050.", 9, 15, true, laptop),
                    new Product(null, "iPad Air M2 11 inch", 16990000, "Tablet cho hoc tap va cong viec.", 20, 7, false, tablet),
                    new Product(null, "Galaxy Tab S9 FE", 10990000, "But S-Pen di kem.", 16, 10, false, tablet),
                    new Product(null, "Apple Watch Series 9", 9990000, "Theo doi suc khoe va thong bao.", 22, 18, true, watch),
                    new Product(null, "Garmin Forerunner 255", 6990000, "Dong ho chay bo GPS chuyen sau.", 8, 0, false, watch),
                    new Product(null, "AirPods Pro 2 USB-C", 5690000, "Chong on chu dong ANC.", 30, 20, true, accessory),
                    new Product(null, "Sac du phong 20000mAh", 790000, "Cong suat 22.5W, 2 cong USB.", 0, 25, false, accessory),
                    new Product(null, "Loa JBL Flip 6", 2490000, "Am thanh manh, chong nuoc IP67.", 11, 10, true, accessory),
                    new Product(null, "Robot hut bui Xiaomi S10", 5490000, "Hut va lau thong minh.", 7, 12, false, smartHome)));
        };
    }
}
