package com.fintrack.controller;

import com.fintrack.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @ControllerAdvice — глобальный обработчик, применяется ко ВСЕМ контроллерам.
 *
 * Без него Spring показывал бы дефолтную белую страницу ошибки.
 * С ним — мы возвращаем наши красивые шаблоны error/404.html и error/500.html.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleError(Exception ex, Model model) {
        model.addAttribute("message", "Что-то пошло не так. Попробуйте позже.");
        return "error/500";
    }
}
