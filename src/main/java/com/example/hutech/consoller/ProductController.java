package com.example.hutech.consoller;

import com.example.hutech.model.Product;
import com.example.hutech.service.CategoryService;
import com.example.hutech.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String listProducts(@RequestParam(name = "category", required = false) Long categoryId,
            @RequestParam(name = "tab", required = false, defaultValue = "all") String tab,
            Model model) {
        List<Product> productsByCategory = categoryId == null
                ? productService.getAllProducts()
                : productService.getProductsByCategoryId(categoryId);

        String normalizedTab = tab == null ? "all" : tab.toLowerCase(Locale.ROOT);
        if (!normalizedTab.equals("promo") && !normalizedTab.equals("regular")) {
            normalizedTab = "all";
        }

        List<Product> filteredProducts = switch (normalizedTab) {
            case "promo" -> productsByCategory.stream()
                    .filter(Product::isPromotional)
                    .toList();
            case "regular" -> productsByCategory.stream()
                    .filter(product -> !product.isPromotional())
                    .toList();
            default -> productsByCategory;
        };

        long promotionalCount = productsByCategory.stream()
                .filter(Product::isPromotional)
                .count();

        model.addAttribute("products", filteredProducts);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("activeTab", normalizedTab);
        model.addAttribute("totalPromotional", promotionalCount);
        model.addAttribute("totalRegular", productsByCategory.size() - promotionalCount);
        return "/products/products-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "/products/add-product";
    }

    @PostMapping("/add")
    public String addProduct(@Valid @ModelAttribute("product") Product product, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "/products/add-product";
        }
        productService.saveProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "/products/edit-product";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(@PathVariable Long id, @Valid @ModelAttribute("product") Product product,
            @RequestParam(name = "removeImage", required = false, defaultValue = "false") boolean removeImage,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            product.setId(id);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "/products/edit-product";
        }
        if (removeImage) {
            product.setImageUrl(null);
        }
        productService.updateProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }
}
