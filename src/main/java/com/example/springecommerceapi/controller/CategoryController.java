package com.example.springecommerceapi.controller;

import com.example.springecommerceapi.dto.CategoryRequest;
import com.example.springecommerceapi.dto.CategoryResponse;
import com.example.springecommerceapi.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // POST /api/categories
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@RequestBody @Valid CategoryRequest request) {
        return categoryService.create(request);
    }

    // GET /api/categories/{id}
    @GetMapping("/{id}")
    public CategoryResponse getById(@PathVariable Long id) {
        return categoryService.getById(id);
    }

    // GET /api/categories
    @GetMapping
    public List<CategoryResponse> getAll() {
        return categoryService.getAll();
    }

    // PUT /api/categories/{id}
    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id,
                                   @RequestBody @Valid CategoryRequest request) {
        return categoryService.update(id, request);
    }

    // DELETE /api/categories/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}

