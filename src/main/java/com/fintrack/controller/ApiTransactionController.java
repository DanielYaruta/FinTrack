package com.fintrack.controller;

import com.fintrack.dto.TransactionDto;
import com.fintrack.dto.TransactionResponse;
import com.fintrack.service.TransactionService;
import com.fintrack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class ApiTransactionController {

    private final TransactionService transactionService;
    private final UserService        userService;

    public ApiTransactionController(TransactionService transactionService,
                                    UserService userService) {
        this.transactionService = transactionService;
        this.userService        = userService;
    }

    @GetMapping
    public List<TransactionResponse> getAll(Authentication auth) {
        return transactionService.findAllByUserId(currentUserId(auth))
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(
                TransactionResponse.from(transactionService.findByIdAndUserId(id, currentUserId(auth))));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@Valid @RequestBody TransactionDto dto, Authentication auth) {
        return TransactionResponse.from(transactionService.save(dto, currentUserId(auth)));
    }

    @PutMapping("/{id}")
    public TransactionResponse update(@PathVariable Long id,
                                      @Valid @RequestBody TransactionDto dto,
                                      Authentication auth) {
        return TransactionResponse.from(transactionService.update(id, dto, currentUserId(auth)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        transactionService.deleteById(id, currentUserId(auth));
    }

    private Long currentUserId(Authentication auth) {
        return userService.findByEmail(auth.getName()).getId();
    }
}
