package com.example.hutech.consoller;

import com.example.hutech.model.User;
import com.example.hutech.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "/auth/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "/auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
            BindingResult result,
            @RequestParam(name = "confirmPassword", required = false) String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "/auth/register";
        }

        if (confirmPassword == null || !confirmPassword.equals(user.getPassword())) {
            model.addAttribute("error", "Xac nhan mat khau khong khop");
            return "/auth/register";
        }

        try {
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Dang ky thanh cong. Vui long dang nhap.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "/auth/register";
        }
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        try {
            User user = userService.getUserByUsername(username.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Username hoac mat khau khong dung"));

            if (!user.isActive()) {
                throw new IllegalArgumentException("Tai khoan da bi khoa");
            }

            if (!userService.verifyPassword(user, password)) {
                throw new IllegalArgumentException("Username hoac mat khau khong dung");
            }

            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());

            String redirectAfterLogin = (String) session.getAttribute("redirectAfterLogin");
            if (redirectAfterLogin != null && !redirectAfterLogin.isBlank()) {
                session.removeAttribute("redirectAfterLogin");
                return "redirect:" + redirectAfterLogin;
            }
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "/auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "Dang xuat thanh cong.");
        return "redirect:/auth/login";
    }
}
