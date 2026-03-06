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
    private static final String SESSION_GUEST_CART_ID = "guestCartId";

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
            model.addAttribute("isAdmin", false);
            model.addAttribute("currentUsername", null);
            model.addAttribute("cartItemCount", getGuestCartItemCount(session));
            return;
        }

        User user = userService.getUserById(userId).orElse(null);
        if (user == null) {
            session.invalidate();
            model.addAttribute("loggedIn", false);
            model.addAttribute("isAdmin", false);
            model.addAttribute("currentUsername", null);
            model.addAttribute("cartItemCount", getGuestCartItemCount(session));
            return;
        }

        boolean isAdmin = userService.isAdmin(user);
        model.addAttribute("loggedIn", true);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("currentUsername", user.getUsername());
        model.addAttribute("cartItemCount", cartService.getTotalItemCount(user));
        session.setAttribute("isAdmin", isAdmin);
    }

    private long getGuestCartItemCount(HttpSession session) {
        Long guestCartId = (Long) session.getAttribute(SESSION_GUEST_CART_ID);
        if (guestCartId == null) {
            return 0L;
        }

        return cartService.getCartById(guestCartId)
                .map(cartService::getTotalItemCount)
                .orElseGet(() -> {
                    session.removeAttribute(SESSION_GUEST_CART_ID);
                    return 0L;
                });
    }
}
