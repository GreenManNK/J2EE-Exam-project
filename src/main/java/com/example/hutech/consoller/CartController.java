package com.example.hutech.consoller;

import com.example.hutech.model.Cart;
import com.example.hutech.model.Order;
import com.example.hutech.model.User;
import com.example.hutech.service.CartService;
import com.example.hutech.service.OrderService;
import com.example.hutech.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        if (user == null) {
            session.setAttribute("redirectAfterLogin", "/cart");
            return "redirect:/auth/login";
        }

        Cart cart = cartService.getOrCreateCart(user);
        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", cart.getTotalPrice());
        return "/cart/view-cart";
    }

    @PostMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(name = "redirect", required = false) String redirect,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(session);
        if (user == null) {
            session.setAttribute("redirectAfterLogin", "/products/" + productId);
            return "redirect:/auth/login";
        }

        try {
            Cart cart = cartService.getOrCreateCart(user);
            cartService.addToCart(cart, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Da them san pham vao gio hang.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        if (redirect != null && redirect.startsWith("/")) {
            return "redirect:" + redirect;
        }
        return "redirect:/products/" + productId;
    }

    @PostMapping("/remove/{itemId}")
    public String removeFromCart(@PathVariable Long itemId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(session);
        if (user == null) {
            session.setAttribute("redirectAfterLogin", "/cart");
            return "redirect:/auth/login";
        }

        Cart cart = cartService.getOrCreateCart(user);
        boolean itemBelongsToCurrentUser = cartService.getCartItemById(itemId)
                .map(item -> item.getCart() != null && item.getCart().getId().equals(cart.getId()))
                .orElse(false);

        if (itemBelongsToCurrentUser) {
            cartService.removeFromCart(itemId);
            redirectAttributes.addFlashAttribute("successMessage", "Da xoa san pham khoi gio hang.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Khong the xoa muc gio hang nay.");
        }

        return "redirect:/cart";
    }

    @PostMapping("/update/{itemId}")
    public String updateQuantity(@PathVariable Long itemId,
            @RequestParam int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(session);
        if (user == null) {
            session.setAttribute("redirectAfterLogin", "/cart");
            return "redirect:/auth/login";
        }

        Cart cart = cartService.getOrCreateCart(user);
        boolean itemBelongsToCurrentUser = cartService.getCartItemById(itemId)
                .map(item -> item.getCart() != null && item.getCart().getId().equals(cart.getId()))
                .orElse(false);

        if (!itemBelongsToCurrentUser) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khong the cap nhat muc gio hang nay.");
            return "redirect:/cart";
        }

        try {
            cartService.updateCartItemQuantity(itemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Da cap nhat so luong.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(session);
        if (user == null) {
            session.setAttribute("redirectAfterLogin", "/cart");
            return "redirect:/auth/login";
        }

        Cart cart = cartService.getOrCreateCart(user);
        cartService.clearCart(cart);
        redirectAttributes.addFlashAttribute("successMessage", "Da xoa toan bo gio hang.");
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(session);
        if (user == null) {
            session.setAttribute("redirectAfterLogin", "/cart");
            return "redirect:/auth/login";
        }

        Cart cart = cartService.getOrCreateCart(user);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Gio hang dang trong.");
            return "redirect:/cart";
        }

        Order order = new Order();
        order.setCustomerName(user.getFullName() == null || user.getFullName().isBlank()
                ? user.getUsername()
                : user.getFullName());
        order.setPhone(user.getPhone() == null ? "" : user.getPhone());
        order.setAddress(user.getAddress() == null ? "" : user.getAddress());
        order.setNotes("Dat tu gio hang");
        order.setTotalPrice(cart.getTotalPrice());
        order.setStatus("pending");
        orderService.saveOrder(order);

        cartService.clearCart(cart);
        redirectAttributes.addFlashAttribute("successMessage", "Dat hang thanh cong. Ma don: #" + order.getId());
        return "redirect:/orders";
    }

    private User getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return null;
        }

        var userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return null;
        }

        return userOpt.get();
    }
}
