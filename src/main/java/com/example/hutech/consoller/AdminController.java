package com.example.hutech.consoller;

import com.example.hutech.model.Product;
import com.example.hutech.service.CategoryService;
import com.example.hutech.service.ProductService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String dashboard(Model model) {
        List<Product> products = productService.getAllProducts();

        long promotionalCount = products.stream()
                .filter(Product::isPromotional)
                .count();

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("productCount", products.size());
        model.addAttribute("promotionalCount", promotionalCount);
        model.addAttribute("regularCount", products.size() - promotionalCount);
        return "/admin/dashboard";
    }
}
