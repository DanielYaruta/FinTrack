package com.fintrack.controller;

import com.fintrack.dto.TransactionDto;
import com.fintrack.dto.TransactionResponse;
import com.fintrack.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @RestController = @Controller + @ResponseBody на каждом методе.
 * Spring автоматически сериализует возвращаемые объекты в JSON через Jackson.
 *
 * Разница от обычного @Controller:
 *   @Controller    → метод возвращает строку-имя шаблона → Thymeleaf рендерит HTML
 *   @RestController → метод возвращает объект → Jackson сериализует в JSON
 */
@RestController
@RequestMapping("/api/transactions")
public class ApiTransactionController {

    private static final Long DEMO_USER_ID = 1L;

    private final TransactionService transactionService;

    public ApiTransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * GET /api/transactions → JSON-массив всех транзакций текущего пользователя.
     * Попробуй в браузере или curl: curl http://localhost:8080/api/transactions
     */
    @GetMapping
    public List<TransactionResponse> getAll() {
        return transactionService.findAllByUserId(DEMO_USER_ID)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    /**
     * GET /api/transactions/{id} → одна транзакция.
     * ResponseEntity позволяет явно задать HTTP-статус и заголовки.
     * Если транзакция не найдена — ResourceNotFoundException → @ControllerAdvice вернёт 404.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id) {
        TransactionResponse response = TransactionResponse.from(
                transactionService.findById(id));
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/transactions — создать транзакцию через JSON.
     * @RequestBody — тело запроса десериализуется из JSON в TransactionDto.
     * @Valid — запускает Bean Validation (те же правила, что и для HTML-формы).
     *
     * Пример запроса:
     *   curl -X POST http://localhost:8080/api/transactions \
     *     -H "Content-Type: application/json" \
     *     -d '{"amount":5000,"type":"EXPENSE","categoryId":4,"date":"2024-06-15","description":"Тест"}'
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)   // HTTP 201 вместо 200
    public TransactionResponse create(@Valid @RequestBody TransactionDto dto) {
        return TransactionResponse.from(
                transactionService.save(dto, DEMO_USER_ID));
    }

    /**
     * DELETE /api/transactions/{id} — REST-клиенты могут использовать DELETE,
     * тогда как Thymeleaf-формы используют POST /transactions/{id}/delete.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)  // HTTP 204 — успешно, тела нет
    public void delete(@PathVariable Long id) {
        transactionService.deleteById(id, DEMO_USER_ID);
    }
}
