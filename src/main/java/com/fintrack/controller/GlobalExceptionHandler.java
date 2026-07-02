package com.fintrack.controller;

import com.fintrack.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @ControllerAdvice — глобальный обработчик, применяется ко ВСЕМ контроллерам.
 *
 * Без него Spring показывал бы дефолтную белую страницу ошибки.
 * С ним — мы возвращаем наши красивые шаблоны error/404.html и error/500.html.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("Resource not found: {}", ex.getMessage());
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    /**
     * Валидация @RequestBody в REST-контроллерах.
     *
     * Когда @Valid + @RequestBody не проходит проверку, Spring бросает
     * MethodArgumentNotValidException. Без этого обработчика его перехватил бы
     * handleError(Exception) и вернул HTML-страницу 500 — неправильно для REST.
     *
     * @ResponseBody — явно говорим Spring сериализовать Map в JSON, а не искать view.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return errors;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleError(Exception ex, Model model) {
        log.error("Unexpected error", ex);
        model.addAttribute("message", "Что-то пошло не так. Попробуйте позже.");
        return "error/500";
    }
}
