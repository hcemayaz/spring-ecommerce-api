package com.example.springecommerceapi.function;

import com.example.springecommerceapi.dto.ProductResponse;
import com.example.springecommerceapi.dto.ProductSearchRequest;
import com.example.springecommerceapi.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSearchFunctionConfig Tests")
class ProductSearchFunctionConfigTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductSearchFunctionConfig config;

    @Test
    @DisplayName("Should search products with given criteria")
    void productSearchFunction_ShouldDelegateToService() {
        ProductSearchRequest request = new ProductSearchRequest("laptop", BigDecimal.valueOf(100), BigDecimal.valueOf(5000), true);

        List<ProductResponse> expected = List.of(
                ProductResponse.builder().id(1L).name("Laptop").build()
        );

        when(productService.searchProducts("laptop", BigDecimal.valueOf(100), BigDecimal.valueOf(5000), true))
                .thenReturn(expected);

        Function<ProductSearchRequest, List<ProductResponse>> function = config.productSearchFunction();
        List<ProductResponse> result = function.apply(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Laptop");
        verify(productService).searchProducts("laptop", BigDecimal.valueOf(100), BigDecimal.valueOf(5000), true);
    }
}
