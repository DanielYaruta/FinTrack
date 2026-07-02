package com.fintrack.controller;

import com.fintrack.dto.TransactionDto;
import com.fintrack.model.Transaction;
import com.fintrack.service.CategoryService;
import com.fintrack.service.TransactionService;
import com.fintrack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
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

    private final TransactionService transactionService;
    private final CategoryService    categoryService;
    private final UserService        userService;

    public TransactionController(TransactionService transactionService,
                                 CategoryService categoryService,
                                 UserService userService) {
        this.transactionService = transactionService;
        this.categoryService    = categoryService;
        this.userService        = userService;
    }

    @GetMapping
    public String list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> to,
            Authentication auth,
            Model model) {

        Long userId = currentUserId(auth);

        List<Transaction> transactions;
        if (from.isPresent() && to.isPresent()) {
            transactions = transactionService.findByUserIdAndDateBetween(userId, from.get(), to.get());
        } else {
            transactions = transactionService.findAllByUserId(userId);
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

    @PostMapping
    public String create(@Valid @ModelAttribute("transactionDto") TransactionDto dto,
                         BindingResult errors,
                         Authentication auth,
                         Model model,
                         RedirectAttributes ra) {
        Long userId = currentUserId(auth);

        if (errors.hasErrors()) {
            model.addAttribute("transactions", transactionService.findAllByUserId(userId));
            model.addAttribute("categories", categoryService.findAll());
            return "transactions";
        }
        transactionService.save(dto, userId);
        ra.addFlashAttribute("success", "Транзакция успешно добавлена");
        return "redirect:/transactions";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication auth, Model model) {
        Transaction tx = transactionService.findByIdAndUserId(id, currentUserId(auth));

        TransactionDto dto = new TransactionDto();
        dto.setAmount(tx.getAmount());
        dto.setType(tx.getType());
        dto.setCategoryId(tx.getCategory() != null ? tx.getCategory().getId() : null);
        dto.setDate(tx.getDate());
        dto.setDescription(tx.getDescription());

        model.addAttribute("transactionDto", dto);
        model.addAttribute("editId", id);
        model.addAttribute("transactions", transactionService.findAllByUserId(currentUserId(auth)));
        model.addAttribute("categories", categoryService.findAll());
        return "transactions";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("transactionDto") TransactionDto dto,
                         BindingResult errors,
                         Authentication auth,
                         Model model,
                         RedirectAttributes ra) {
        Long userId = currentUserId(auth);

        if (errors.hasErrors()) {
            model.addAttribute("transactions", transactionService.findAllByUserId(userId));
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("editId", id);
            return "transactions";
        }
        transactionService.update(id, dto, userId);
        ra.addFlashAttribute("success", "Транзакция обновлена");
        return "redirect:/transactions";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        transactionService.deleteById(id, currentUserId(auth));
        ra.addFlashAttribute("success", "Транзакция удалена");
        return "redirect:/transactions";
    }

    private Long currentUserId(Authentication auth) {
        return userService.findByEmail(auth.getName()).getId();
    }
}
