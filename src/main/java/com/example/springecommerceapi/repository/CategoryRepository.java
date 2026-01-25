package com.example.springecommerceapi.repository;

import com.example.springecommerceapi.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
