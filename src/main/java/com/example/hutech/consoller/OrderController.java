package com.example.hutech.consoller;

import com.example.hutech.model.Order;
import com.example.hutech.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {
 @Autowired
 private OrderService orderService;
    
 @GetMapping
 public String listOrders(Model model) {
 List<Order> orders = orderService.getAllOrders();
 model.addAttribute("orders", orders);
 return "/orders/orders-list";
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
}
