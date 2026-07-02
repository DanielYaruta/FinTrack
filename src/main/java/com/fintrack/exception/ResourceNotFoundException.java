package com.fintrack.exception;

/**
 * Бросается когда сущность не найдена в БД (аналог HTTP 404).
 *
 * Наследуем RuntimeException, а не Exception — чтобы не засорять сигнатуры методов
 * checked-исключением. Spring сам поймает его и вернёт 404 (в Stage 3 мы
 * добавим @ControllerAdvice для красивой страницы ошибки).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String entity, Long id) {
        return new ResourceNotFoundException(entity + " с id=" + id + " не найден");
    }
}
