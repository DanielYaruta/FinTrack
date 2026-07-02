package com.fintrack.controller;

import com.fintrack.dto.CategoryDto;
import com.fintrack.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        if (!model.containsAttribute("categoryDto")) {
            model.addAttribute("categoryDto", new CategoryDto());
        }
        model.addAttribute("categories", categoryService.findAll());
        return "categories";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("categoryDto") CategoryDto dto,
                         BindingResult errors,
                         Model model,
                         RedirectAttributes ra) {
        if (errors.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "categories";
        }
        categoryService.save(dto);
        ra.addFlashAttribute("success", "Категория «" + dto.getName() + "» добавлена");
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        categoryService.deleteById(id);
        ra.addFlashAttribute("success", "Категория удалена");
        return "redirect:/categories";
    }
}
