package com.example.hutech.consoller;

import com.example.hutech.model.Product;
import com.example.hutech.service.CategoryService;
import com.example.hutech.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        var allProducts = productService.getAllProducts();
        List<Product> promotionalProducts = new ArrayList<>();
        List<Product> regularProducts = new ArrayList<>();

        for (var product : allProducts) {
            if (product.isPromotional()) {
                promotionalProducts.add(product);
            } else {
                regularProducts.add(product);
            }
        }

        model.addAttribute("products", allProducts);
        model.addAttribute("promotionalProducts", promotionalProducts);
        model.addAttribute("regularProducts", regularProducts);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "index";
    }

    @RequestMapping("/product/**")
    public String redirectToProducts(HttpServletRequest request) {
        String path = request.getRequestURI().substring(8);
        return "redirect:/products" + path;
    }

    @RequestMapping("/category/**")
    public String redirectToCategories(HttpServletRequest request) {
        String path = request.getRequestURI().substring(9);
        return "redirect:/categories" + path;
    }

    @RequestMapping("/order/**")
    public String redirectToOrders(HttpServletRequest request) {
        String path = request.getRequestURI().substring(6);
        return "redirect:/orders" + path;
    }
}
