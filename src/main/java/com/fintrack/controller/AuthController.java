package com.fintrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    /**
     * GET /login — показываем форму входа.
     * POST /login — обрабатывает Spring Security автоматически, нам не нужно писать этот метод.
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
