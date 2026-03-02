package com.example.springecommerceapi.function;

import com.example.springecommerceapi.dto.ProductResponse;
import com.example.springecommerceapi.dto.ProductSearchRequest;
import com.example.springecommerceapi.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ProductSearchFunctionConfig {

    private final ProductService productService;

    @Bean
    @Description("Katalogdan ürün aramak için kullanılır. İstenilen anahtar kelime, minimum fiyat, maksimum fiyat veya stok durumu (true/false) kriterlerine göre filtreleme yapar. Kullanıcı 'ucuz', 'pahalı' veya 'oyuncu' gibi şeyler aradığında bu tool'u kullanarak veritabanındaki ürünleri sorgula.")
    public Function<ProductSearchRequest, List<ProductResponse>> productSearchFunction() {
        return request -> {
            log.info("AI is searching products with criteria: {}", request);
            return productService.searchProducts(
                    request.keyword(),
                    request.minPrice(),
                    request.maxPrice(),
                    request.inStock());
        };
    }
}
