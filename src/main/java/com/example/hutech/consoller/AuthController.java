package com.example.hutech.consoller;

import com.example.hutech.model.Cart;
import com.example.hutech.model.User;
import com.example.hutech.service.CartService;
import com.example.hutech.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String SESSION_GUEST_CART_ID = "guestCartId";
    private static final String SESSION_FACEBOOK_STATE = "facebookOauthState";

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Value("${app.auth.facebook.client-id:}")
    private String facebookClientId;

    @Value("${app.auth.facebook.client-secret:}")
    private String facebookClientSecret;

    @Value("${app.auth.facebook.redirect-uri:}")
    private String facebookRedirectUri;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

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
    public String loginUser(@RequestParam String identifier,
            @RequestParam String password,
            HttpSession session,
            Model model) {
        try {
            User user = userService.findByLoginIdentifier(identifier)
                    .orElseThrow(() -> new IllegalArgumentException("Thong tin dang nhap hoac mat khau khong dung"));

            if (!user.isActive()) {
                throw new IllegalArgumentException("Tai khoan da bi khoa");
            }

            if (!userService.verifyPassword(user, password)) {
                throw new IllegalArgumentException("Thong tin dang nhap hoac mat khau khong dung");
            }

            setAuthenticatedSession(session, user);
            mergeGuestCartAfterLogin(session, user);

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

    @GetMapping("/facebook")
    public String loginWithFacebook(HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isFacebookConfigured()) {
            redirectAttributes.addFlashAttribute("error", "Facebook login chua duoc cau hinh.");
            return "redirect:/auth/login";
        }

        String state = UUID.randomUUID().toString();
        session.setAttribute(SESSION_FACEBOOK_STATE, state);

        String facebookAuthUrl = "https://www.facebook.com/v20.0/dialog/oauth"
                + "?client_id=" + urlEncode(facebookClientId)
                + "&redirect_uri=" + urlEncode(facebookRedirectUri)
                + "&state=" + urlEncode(state)
                + "&scope=" + urlEncode("email,public_profile");

        return "redirect:" + facebookAuthUrl;
    }

    @GetMapping("/facebook/callback")
    public String facebookCallback(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (!isFacebookConfigured()) {
            redirectAttributes.addFlashAttribute("error", "Facebook login chua duoc cau hinh.");
            return "redirect:/auth/login";
        }

        if (error != null && !error.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Dang nhap Facebook bi huy hoac that bai.");
            return "redirect:/auth/login";
        }

        String sessionState = (String) session.getAttribute(SESSION_FACEBOOK_STATE);
        session.removeAttribute(SESSION_FACEBOOK_STATE);
        if (sessionState == null || state == null || !sessionState.equals(state)) {
            redirectAttributes.addFlashAttribute("error", "Phien dang nhap Facebook khong hop le.");
            return "redirect:/auth/login";
        }

        if (code == null || code.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Khong nhan duoc ma xac thuc Facebook.");
            return "redirect:/auth/login";
        }

        try {
            String accessToken = exchangeFacebookAccessToken(code);
            FacebookProfile profile = fetchFacebookProfile(accessToken);
            User user = userService.getOrCreateFacebookUser(profile.id(), profile.name(), profile.email());
            setAuthenticatedSession(session, user);
            mergeGuestCartAfterLogin(session, user);

            String redirectAfterLogin = (String) session.getAttribute("redirectAfterLogin");
            if (redirectAfterLogin != null && !redirectAfterLogin.isBlank()) {
                session.removeAttribute("redirectAfterLogin");
                return "redirect:" + redirectAfterLogin;
            }
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Dang nhap Facebook that bai. Vui long thu lai.");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "Dang xuat thanh cong.");
        return "redirect:/auth/login";
    }

    private void setAuthenticatedSession(HttpSession session, User user) {
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("isAdmin", userService.isAdmin(user));
    }

    private void mergeGuestCartAfterLogin(HttpSession session, User user) {
        Long guestCartId = (Long) session.getAttribute(SESSION_GUEST_CART_ID);
        if (guestCartId == null) {
            return;
        }

        Cart userCart = cartService.getOrCreateCart(user);
        cartService.getCartById(guestCartId)
                .ifPresent(guestCart -> cartService.mergeCarts(guestCart, userCart));

        session.removeAttribute(SESSION_GUEST_CART_ID);
    }

    private boolean isFacebookConfigured() {
        return facebookClientId != null && !facebookClientId.isBlank()
                && facebookClientSecret != null && !facebookClientSecret.isBlank()
                && facebookRedirectUri != null && !facebookRedirectUri.isBlank();
    }

    private String exchangeFacebookAccessToken(String code) throws IOException, InterruptedException {
        String tokenUrl = "https://graph.facebook.com/v20.0/oauth/access_token"
                + "?client_id=" + urlEncode(facebookClientId)
                + "&client_secret=" + urlEncode(facebookClientSecret)
                + "&redirect_uri=" + urlEncode(facebookRedirectUri)
                + "&code=" + urlEncode(code);

        HttpRequest request = HttpRequest.newBuilder(URI.create(tokenUrl))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new IllegalArgumentException("Khong lay duoc access token tu Facebook.");
        }

        JsonNode json = objectMapper.readTree(response.body());
        String accessToken = json.path("access_token").asText(null);
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Khong lay duoc access token tu Facebook.");
        }

        return accessToken;
    }

    private FacebookProfile fetchFacebookProfile(String accessToken) throws IOException, InterruptedException {
        String profileUrl = "https://graph.facebook.com/me"
                + "?fields=" + urlEncode("id,name,email")
                + "&access_token=" + urlEncode(accessToken);

        HttpRequest request = HttpRequest.newBuilder(URI.create(profileUrl))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new IllegalArgumentException("Khong lay duoc thong tin tai khoan Facebook.");
        }

        JsonNode json = objectMapper.readTree(response.body());
        String id = json.path("id").asText(null);
        String name = json.path("name").asText(null);
        String email = json.path("email").asText(null);

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Thong tin tai khoan Facebook khong hop le.");
        }

        return new FacebookProfile(id, name, email);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record FacebookProfile(String id, String name, String email) {
    }
}
