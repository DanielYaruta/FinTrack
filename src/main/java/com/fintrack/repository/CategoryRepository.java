package com.fintrack.repository;

import com.fintrack.model.Category;
import com.fintrack.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // SELECT * FROM categories WHERE type = ?
    List<Category> findByType(TransactionType type);
}
