package com.fintrack.dto;

import com.fintrack.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CategoryDto {

    @NotBlank(message = "Название категории обязательно")
    @Size(max = 100, message = "Название не более 100 символов")
    private String name;

    @NotNull(message = "Тип категории обязателен")
    private TransactionType type;

    public CategoryDto() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
}
