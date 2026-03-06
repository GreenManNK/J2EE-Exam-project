package com.example.hutech.config;

import com.example.hutech.model.User;
import com.example.hutech.service.CategoryService;
import com.example.hutech.service.CartService;
import com.example.hutech.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalViewModelAdvice {
    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CategoryService categoryService;

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpSession session) {
        model.addAttribute("categories", categoryService.getAllCategories());

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            model.addAttribute("loggedIn", false);
            model.addAttribute("currentUsername", null);
            model.addAttribute("cartItemCount", 0L);
            return;
        }

        User user = userService.getUserById(userId).orElse(null);
        if (user == null) {
            session.invalidate();
            model.addAttribute("loggedIn", false);
            model.addAttribute("currentUsername", null);
            model.addAttribute("cartItemCount", 0L);
            return;
        }

        model.addAttribute("loggedIn", true);
        model.addAttribute("currentUsername", user.getUsername());
        model.addAttribute("cartItemCount", cartService.getTotalItemCount(user));
    }
}
