package com.fintrack.controller;

import com.fintrack.model.User;
import com.fintrack.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String show(Authentication auth, Model model) {
        model.addAttribute("user", userService.findByEmail(auth.getName()));
        return "profile";
    }

    @PostMapping
    public String update(Authentication auth,
                         @RequestParam String name,
                         @RequestParam String email,
                         RedirectAttributes ra) {
        User user = userService.findByEmail(auth.getName());
        userService.update(user.getId(), name, email);
        ra.addFlashAttribute("success", "Профиль обновлён");
        return "redirect:/profile";
    }
}
