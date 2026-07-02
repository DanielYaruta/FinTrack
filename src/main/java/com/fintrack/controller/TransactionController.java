package com.fintrack.controller;

import com.fintrack.dto.TransactionDto;
import com.fintrack.model.Transaction;
import com.fintrack.service.CategoryService;
import com.fintrack.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private static final Long DEMO_USER_ID = 1L;

    private final TransactionService transactionService;
    private final CategoryService categoryService;

    public TransactionController(TransactionService transactionService,
                                 CategoryService categoryService) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
    }

    /**
     * GET /transactions — список транзакций + форма добавления.
     *
     * @RequestParam с Optional — параметр необязателен. Если from/to не переданы,
     * показываем все транзакции; если переданы — фильтруем за период.
     *
     * model.containsAttribute("transactionDto") — проверяем, не пришёл ли dto
     * из предыдущего POST с ошибками (через FlashAttribute). Если пришёл —
     * не перезаписываем его новым пустым объектом, чтобы форма сохранила введённые значения.
     */
    @GetMapping
    public String list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> to,
            Model model) {

        List<Transaction> transactions;
        if (from.isPresent() && to.isPresent()) {
            transactions = transactionService.findByUserIdAndDateBetween(
                    DEMO_USER_ID, from.get(), to.get());
        } else {
            transactions = transactionService.findAllByUserId(DEMO_USER_ID);
        }

        if (!model.containsAttribute("transactionDto")) {
            TransactionDto dto = new TransactionDto();
            dto.setDate(LocalDate.now());
            model.addAttribute("transactionDto", dto);
        }

        model.addAttribute("transactions", transactions);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("from", from.orElse(null));
        model.addAttribute("to", to.orElse(null));
        return "transactions";
    }

    /**
     * POST /transactions — создание новой транзакции.
     *
     * @Valid — запускает Bean Validation на dto.
     * BindingResult — держит результаты валидации. ВАЖНО: он должен идти сразу
     *   после @Valid параметра, иначе Spring бросит исключение вместо передачи ошибок.
     *
     * Паттерн PRG (Post/Redirect/Get): после успешного сохранения — redirect,
     *   чтобы F5 не создавал дублирующую транзакцию.
     */
    @PostMapping
    public String create(@Valid @ModelAttribute("transactionDto") TransactionDto dto,
                         BindingResult errors,
                         Model model,
                         RedirectAttributes ra) {
        if (errors.hasErrors()) {
            // Возвращаем страницу с ошибками — форма уже содержит введённые данные
            model.addAttribute("transactions", transactionService.findAllByUserId(DEMO_USER_ID));
            model.addAttribute("categories", categoryService.findAll());
            return "transactions";
        }
        transactionService.save(dto, DEMO_USER_ID);
        ra.addFlashAttribute("success", "Транзакция успешно добавлена");
        return "redirect:/transactions";
    }

    /**
     * POST /transactions/{id}/delete — удаление.
     * HTML-формы поддерживают только GET и POST, поэтому удаление — через POST на отдельный URL.
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        transactionService.deleteById(id, DEMO_USER_ID);
        ra.addFlashAttribute("success", "Транзакция удалена");
        return "redirect:/transactions";
    }
}
