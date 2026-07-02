package com.fintrack.controller;

import com.fintrack.model.User;
import com.fintrack.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private static final Long DEMO_USER_ID = 1L;

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String show(Model model) {
        model.addAttribute("user", userService.findById(DEMO_USER_ID));
        return "profile";
    }

    @PostMapping
    public String update(@RequestParam String name,
                         @RequestParam String email,
                         RedirectAttributes ra) {
        userService.update(DEMO_USER_ID, name, email);
        ra.addFlashAttribute("success", "Профиль обновлён");
        return "redirect:/profile";
    }
}
