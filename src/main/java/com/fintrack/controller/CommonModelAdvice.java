package com.fintrack.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Добавляет в модель каждого запроса:
 *   currentUri  — URI текущего запроса (для подсветки активного пункта меню)
 *   currentUserEmail — email залогиненного пользователя (для отображения в navbar)
 *
 * Thymeleaf 3.1 запретил #request в шаблонных выражениях,
 * поэтому передаём эти данные через модель.
 */
@ControllerAdvice
public class CommonModelAdvice {

    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    /**
     * Имя пользователя (email) для отображения в навбаре.
     * Authentication — Spring MVC внедряет автоматически из SecurityContext.
     * Никаких обращений к БД — email уже хранится в токене аутентификации.
     */
    @ModelAttribute("currentUserEmail")
    public String currentUserEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return authentication.getName();
    }
}
