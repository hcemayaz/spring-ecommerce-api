package com.example.springecommerceapi.service;

import com.example.springecommerceapi.domain.Category;
import com.example.springecommerceapi.domain.Product;
import com.example.springecommerceapi.dto.ProductRequest;
import com.example.springecommerceapi.dto.ProductResponse;
import com.example.springecommerceapi.exception.BusinessException;
import com.example.springecommerceapi.exception.NotFoundException;
import com.example.springecommerceapi.repository.CategoryRepository;
import com.example.springecommerceapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Category electronicsCategory;

    @BeforeEach
    void setUp() {
        electronicsCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();
    }

    private ProductRequest createDefaultRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("iPhone 15");
        request.setSku("IPHONE-15-BLACK-128");
        request.setPrice(BigDecimal.valueOf(49999.90));
        request.setStockQuantity(10);
        request.setActive(true);
        request.setCategoryId(1L);
        return request;
    }

    private Product createDefaultProduct() {
        Product product = Product.builder()
                .id(1L)
                .name("iPhone 15")
                .sku("IPHONE-15-BLACK-128")
                .price(BigDecimal.valueOf(49999.90))
                .stockQuantity(10)
                .active(true)
                .category(electronicsCategory)
                .build();
        return product;
    }

    @Test
    void create_shouldSaveProduct_whenSkuIsUnique() {

        ProductRequest request = createDefaultRequest();

        when(productRepository.existsBySku(request.getSku())).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronicsCategory));

        Product saved = createDefaultProduct();
        when(productRepository.save(any(Product.class))).thenReturn(saved);


        ProductResponse response = productService.create(request);


        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("iPhone 15");
        assertThat(response.getSku()).isEqualTo("IPHONE-15-BLACK-128");
        assertThat(response.getCategoryId()).isEqualTo(1L);
        assertThat(response.getCategoryName()).isEqualTo("Electronics");


        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product captured = productCaptor.getValue();

        assertThat(captured.getName()).isEqualTo(request.getName());
        assertThat(captured.getSku()).isEqualTo(request.getSku());
        assertThat(captured.getPrice()).isEqualTo(request.getPrice());
        assertThat(captured.getStockQuantity()).isEqualTo(request.getStockQuantity());
        assertThat(captured.getActive()).isTrue();
        assertThat(captured.getCategory().getId()).isEqualTo(1L);

        verify(productRepository).existsBySku(request.getSku());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void create_shouldThrowBusinessException_whenSkuAlreadyExists() {

        ProductRequest request = createDefaultRequest();

        when(productRepository.existsBySku(request.getSku())).thenReturn(true);


        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> productService.create(request)
        );


        assertThat(ex.getMessage()).contains("already exists");
        verify(productRepository, never()).save(any());
        verify(categoryRepository, never()).findById(anyLong());
    }

    @Test
    void getById_shouldReturnProduct_whenProductExists() {

        Product product = createDefaultProduct();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));


        ProductResponse response = productService.getById(1L);


        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("iPhone 15");
        assertThat(response.getSku()).isEqualTo("IPHONE-15-BLACK-128");
        assertThat(response.getCategoryName()).isEqualTo("Electronics");

        verify(productRepository).findById(1L);
    }

    @Test
    void getById_shouldThrowNotFound_whenProductDoesNotExist() {

        when(productRepository.findById(99L)).thenReturn(Optional.empty());


        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> productService.getById(99L)
        );


        assertThat(ex.getMessage()).contains("Product not found");
        verify(productRepository).findById(99L);
    }

    @Test
    void getAll_shouldReturnAllProducts() {

        Product product1 = createDefaultProduct();
        Product product2 = Product.builder()
                .id(2L)
                .name("MacBook Pro")
                .sku("MACBOOK-PRO-14")
                .price(BigDecimal.valueOf(79999.90))
                .stockQuantity(5)
                .active(true)
                .category(electronicsCategory)
                .build();

        when(productRepository.findAll()).thenReturn(List.of(product1, product2));


        List<ProductResponse> responses = productService.getAll();


        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(ProductResponse::getId)
                .containsExactlyInAnyOrder(1L, 2L);

        verify(productRepository).findAll();
    }

    @Test
    void update_shouldUpdateProduct_whenDataIsValidAndSkuNotTaken() {

        Product existing = createDefaultProduct(); // sku: IPHONE-15-BLACK-128

        ProductRequest request = createDefaultRequest();
        request.setName("iPhone 15 Pro");
        request.setPrice(BigDecimal.valueOf(59999.90));
        request.setStockQuantity(7);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronicsCategory));

        Product updated = Product.builder()
                .id(1L)
                .name("iPhone 15 Pro")
                .sku(existing.getSku())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .active(request.getActive())
                .category(electronicsCategory)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(updated);


        ProductResponse response = productService.update(1L, request);


        assertThat(response.getName()).isEqualTo("iPhone 15 Pro");
        assertThat(response.getPrice()).isEqualTo(request.getPrice());
        assertThat(response.getStockQuantity()).isEqualTo(request.getStockQuantity());

        verify(productRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void update_shouldThrowBusinessException_whenSkuChangedToExistingOne() {

        Product existing = createDefaultProduct(); // sku: IPHONE-15-BLACK-128

        ProductRequest request = createDefaultRequest();
        request.setSku("EXISTING-SKU");

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.existsBySku("EXISTING-SKU")).thenReturn(true);


        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> productService.update(1L, request)
        );


        assertThat(ex.getMessage()).contains("already exists");
        verify(productRepository).findById(1L);
        verify(productRepository).existsBySku("EXISTING-SKU");
        verify(productRepository, never()).save(any());
    }

    @Test
    void delete_shouldDelete_whenProductExists() {

        when(productRepository.existsById(1L)).thenReturn(true);


        productService.delete(1L);


        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowNotFound_whenProductDoesNotExist() {

        when(productRepository.existsById(99L)).thenReturn(false);


        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> productService.delete(99L)
        );


        assertThat(ex.getMessage()).contains("Product not found");
        verify(productRepository).existsById(99L);
        verify(productRepository, never()).deleteById(anyLong());
    }
}
