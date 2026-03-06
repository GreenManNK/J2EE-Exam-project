package com.example.hutech.consoller;
import com.example.hutech.model.Category;
import com.example.hutech.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoryController {
 @Autowired
 private CategoryService categoryService;
 
 // Hiển thị danh sách danh mục
 @GetMapping
 public String listCategories(Model model) {
 List<Category> categories = categoryService.getAllCategories();
 model.addAttribute("categories", categories);
 return "/categories/categories-list";
 }
 
 @GetMapping("/add")
 public String showAddForm(Model model) {
 model.addAttribute("category", new Category());
 return "/categories/add-category";
 }
 
 @PostMapping("/add")
 public String addCategory(@Valid Category category, BindingResult result) {
 if (result.hasErrors()) {
 return "/categories/add-category";
 }
 categoryService.addCategory(category);
 return "redirect:/categories";
 }
 
 @GetMapping("/edit/{id}")
 public String showEditForm(@PathVariable Long id, Model model) {
 Category category = categoryService.getCategoryById(id)
 .orElseThrow(() -> new IllegalArgumentException("Invalid category Id:" + id));
 model.addAttribute("category", category);
 return "/categories/edit-category";
 }
 
 @PostMapping("/edit/{id}")
 public String updateCategory(@PathVariable Long id, @Valid Category category, BindingResult result) {
 if (result.hasErrors()) {
 category.setId(id);
 return "/categories/edit-category";
 }
 categoryService.updateCategory(category);
 return "redirect:/categories";
 }
 
 @GetMapping("/delete/{id}")
 public String deleteCategory(@PathVariable Long id) {
 categoryService.deleteCategory(id);
 return "redirect:/categories";
 }
}