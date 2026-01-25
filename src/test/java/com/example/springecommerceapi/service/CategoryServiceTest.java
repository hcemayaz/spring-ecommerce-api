package com.example.springecommerceapi.service;

import com.example.springecommerceapi.domain.Category;
import com.example.springecommerceapi.dto.CategoryRequest;
import com.example.springecommerceapi.dto.CategoryResponse;
import com.example.springecommerceapi.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


// TODO: Add Log4j2-based logging


@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void shouldCreateCategoryWithoutParent() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");

        Category savedCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .parent(null)
                .build();

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(savedCategory);

        CategoryResponse response = categoryService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Electronics");
        assertThat(response.getParentId()).isNull();

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldCreateCategoryWithParent() {
        // given
        Category parent = Category.builder()
                .id(10L)
                .name("Electronics")
                .build();

        CategoryRequest request = new CategoryRequest();
        request.setName("Phones");
        request.setParentId(10L);

        Category savedCategory = Category.builder()
                .id(11L)
                .name("Phones")
                .parent(parent)
                .build();

        when(categoryRepository.findById(10L))
                .thenReturn(Optional.of(parent));

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(savedCategory);

        CategoryResponse response = categoryService.create(request);

        assertThat(response.getId()).isEqualTo(11L);
        assertThat(response.getName()).isEqualTo("Phones");
        assertThat(response.getParentId()).isEqualTo(10L);

        verify(categoryRepository).findById(10L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldGetCategoryById() {
        Category category = Category.builder()
                .id(5L)
                .name("Books")
                .build();

        when(categoryRepository.findById(5L))
                .thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.getById(5L);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getName()).isEqualTo("Books");
        assertThat(response.getParentId()).isNull();

        verify(categoryRepository).findById(5L);
    }
}

