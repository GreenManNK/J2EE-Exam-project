package com.example.hutech.consoller;

import com.example.hutech.model.Order;
import com.example.hutech.model.User;
import com.example.hutech.service.OrderService;
import com.example.hutech.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {
 @Autowired
 private OrderService orderService;

 @Autowired
 private UserService userService;
    
 @GetMapping
 public String listOrders(Model model, HttpSession session) {
 User user = getCurrentUser(session);
 if (user == null) {
 session.setAttribute("redirectAfterLogin", "/orders");
 return "redirect:/auth/login";
 }

 boolean isAdmin = userService.isAdmin(user);
 List<Order> orders = isAdmin ? orderService.getAllOrders() : orderService.getOrdersByUser(user);
 model.addAttribute("orders", orders);
 model.addAttribute("isOrderAdmin", isAdmin);
 return "/orders/orders-list";
 }

 @GetMapping("/{id}")
 public String orderDetail(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
 User user = getCurrentUser(session);
 if (user == null) {
 session.setAttribute("redirectAfterLogin", "/orders/" + id);
 return "redirect:/auth/login";
 }

 Order order = orderService.getOrderForViewer(id, user)
 .orElse(null);
 if (order == null) {
 redirectAttributes.addFlashAttribute("errorMessage", "Khong tim thay don hang hoac ban khong co quyen truy cap.");
 return "redirect:/orders";
 }

 model.addAttribute("order", order);
 model.addAttribute("isOrderAdmin", userService.isAdmin(user));
 return "/orders/order-detail";
 }
    
 @GetMapping("/add")
 public String showAddForm(Model model) {
 model.addAttribute("order", new Order());
 return "/orders/add-order";
 }
    
 @PostMapping("/add")
 public String addOrder(@Valid @ModelAttribute("order") Order order, BindingResult result) {
 if (result.hasErrors()) {
 return "/orders/add-order";
 }
 orderService.saveOrder(order);
 return "redirect:/orders";
 }
 
 @GetMapping("/edit/{id}")
 public String showEditForm(@PathVariable Long id, Model model) {
 Order order = orderService.getOrderById(id)
 .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + id));
 model.addAttribute("order", order);
 return "/orders/edit-order";
 }
 
 @PostMapping("/edit/{id}")
 public String updateOrder(@PathVariable Long id, @Valid @ModelAttribute("order") Order order, BindingResult result) {
 if (result.hasErrors()) {
 order.setId(id);
 return "/orders/edit-order";
 }
 orderService.updateOrder(order);
 return "redirect:/orders";
 }
 
 @GetMapping("/delete/{id}")
 public String deleteOrder(@PathVariable Long id) {
 orderService.deleteOrder(id);
 return "redirect:/orders";
 }

 @PostMapping("/{id}/cancel")
 public String cancelOrder(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
 User user = getCurrentUser(session);
 if (user == null) {
 session.setAttribute("redirectAfterLogin", "/orders/" + id);
 return "redirect:/auth/login";
 }

 try {
 orderService.cancelOrderForUser(id, user);
 redirectAttributes.addFlashAttribute("successMessage", "Da huy don hang #" + id);
 } catch (IllegalArgumentException e) {
 redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
 }
 return "redirect:/orders/" + id;
 }

 private User getCurrentUser(HttpSession session) {
 Long userId = (Long) session.getAttribute("userId");
 if (userId == null) {
 return null;
 }

 return userService.getUserById(userId).orElseGet(() -> {
 session.invalidate();
 return null;
 });
 }
}
