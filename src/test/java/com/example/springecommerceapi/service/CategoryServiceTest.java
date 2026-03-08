package com.example.springecommerceapi.service;

import com.example.springecommerceapi.domain.Category;
import com.example.springecommerceapi.dto.CategoryRequest;
import com.example.springecommerceapi.dto.CategoryResponse;
import com.example.springecommerceapi.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Nested
    @DisplayName("Create operations")
    class CreateTests {

        @Test
        @DisplayName("Should create category without parent")
        void shouldCreateCategoryWithoutParent() {
            CategoryRequest request = new CategoryRequest();
            request.setName("Electronics");

            Category savedCategory = Category.builder()
                    .id(1L)
                    .name("Electronics")
                    .parent(null)
                    .build();

            when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

            CategoryResponse response = categoryService.create(request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("Electronics");
            assertThat(response.getParentId()).isNull();
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("Should create category with parent")
        void shouldCreateCategoryWithParent() {
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

            when(categoryRepository.findById(10L)).thenReturn(Optional.of(parent));
            when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

            CategoryResponse response = categoryService.create(request);

            assertThat(response.getId()).isEqualTo(11L);
            assertThat(response.getName()).isEqualTo("Phones");
            assertThat(response.getParentId()).isEqualTo(10L);
            verify(categoryRepository).findById(10L);
            verify(categoryRepository).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("Read operations")
    class ReadTests {

        @Test
        @DisplayName("Should get category by id")
        void shouldGetCategoryById() {
            Category category = Category.builder()
                    .id(5L)
                    .name("Books")
                    .build();

            when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));

            CategoryResponse response = categoryService.getById(5L);

            assertThat(response.getId()).isEqualTo(5L);
            assertThat(response.getName()).isEqualTo("Books");
            assertThat(response.getParentId()).isNull();
            verify(categoryRepository).findById(5L);
        }

        @Test
        @DisplayName("Should throw NotFoundException when category does not exist")
        void getById_WhenNotFound_ShouldThrow() {
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.getById(99L))
                    .isInstanceOf(com.example.springecommerceapi.exception.NotFoundException.class)
                    .hasMessageContaining("Category not found");
        }

        @Test
        @DisplayName("Should return all categories")
        void getAll_ShouldReturnList() {
            List<Category> categories = List.of(
                    Category.builder().id(1L).name("Electronics").build(),
                    Category.builder().id(2L).name("Books").build()
            );
            when(categoryRepository.findAll()).thenReturn(categories);

            List<CategoryResponse> responses = categoryService.getAll();

            assertThat(responses).hasSize(2);
            verify(categoryRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Update operations")
    class UpdateTests {

        @Test
        @DisplayName("Should update category without parent")
        void update_ShouldUpdateCategory() {
            Category existing = Category.builder().id(1L).name("Electronics").build();
            Category updated = Category.builder().id(1L).name("Updated Electronics").build();

            CategoryRequest request = new CategoryRequest();
            request.setName("Updated Electronics");

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(categoryRepository.save(any(Category.class))).thenReturn(updated);

            CategoryResponse response = categoryService.update(1L, request);

            assertThat(response.getName()).isEqualTo("Updated Electronics");
            assertThat(response.getParentId()).isNull();
        }

        @Test
        @DisplayName("Should update category with parent")
        void update_WithParent_ShouldSetParent() {
            Category existing = Category.builder().id(1L).name("Phones").build();
            Category parent = Category.builder().id(10L).name("Electronics").build();
            Category updated = Category.builder().id(1L).name("Phones").parent(parent).build();

            CategoryRequest request = new CategoryRequest();
            request.setName("Phones");
            request.setParentId(10L);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(categoryRepository.findById(10L)).thenReturn(Optional.of(parent));
            when(categoryRepository.save(any(Category.class))).thenReturn(updated);

            CategoryResponse response = categoryService.update(1L, request);

            assertThat(response.getParentId()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("Delete operations")
    class DeleteTests {

        @Test
        @DisplayName("Should delete category when exists")
        void delete_WhenExists_ShouldDelete() {
            when(categoryRepository.existsById(1L)).thenReturn(true);

            categoryService.delete(1L);

            verify(categoryRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw NotFoundException when category does not exist")
        void delete_WhenNotFound_ShouldThrow() {
            when(categoryRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> categoryService.delete(99L))
                    .isInstanceOf(com.example.springecommerceapi.exception.NotFoundException.class)
                    .hasMessageContaining("Category not found");

            verify(categoryRepository, never()).deleteById(anyLong());
        }
    }
}
