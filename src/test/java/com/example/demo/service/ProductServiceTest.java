package com.example.demo.service;

import com.example.demo.domain.Product;
import com.example.demo.dto.ProductDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product1;
    private Product product2;
    private List<Product> productList;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        product1 = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .build();

        product2 = Product.builder()
                .id(2L)
                .name("Another Product")
                .price(new BigDecimal("49.99"))
                .build();

        productList = Arrays.asList(product1, product2);

        productDTO = ProductDTO.builder()
                .name("New Product")
                .price(new BigDecimal("79.99"))
                .build();
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        when(productRepository.findAll()).thenReturn(productList);

        List<ProductDTO> result = productService.getAllProducts();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Test Product");
        assertThat(result.get(0).getPrice()).isEqualTo(new BigDecimal("99.99"));
        assertThat(result.get(1).getId()).isEqualTo(2L);

        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductById_WithValidId_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        ProductDTO result = productService.getProductById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Product");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("99.99"));

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_WithInvalidId_ShouldThrowException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(999L);
        });

        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() {
        Product productToSave = Product.builder()
                .name("New Product")
                .price(new BigDecimal("79.99"))
                .build();

        Product savedProduct = Product.builder()
                .id(3L)
                .name("New Product")
                .price(new BigDecimal("79.99"))
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductDTO result = productService.createProduct(productDTO);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo("New Product");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("79.99"));

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_WithValidId_ShouldReturnUpdatedProduct() {
        ProductDTO updateDTO = ProductDTO.builder()
                .name("Updated Product")
                .price(new BigDecimal("129.99"))
                .build();

        Product updatedProduct = Product.builder()
                .id(1L)
                .name("Updated Product")
                .price(new BigDecimal("129.99"))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductDTO result = productService.updateProduct(1L, updateDTO);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Updated Product");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("129.99"));

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_WithInvalidId_ShouldThrowException() {
        ProductDTO updateDTO = ProductDTO.builder()
                .name("Updated Product")
                .price(new BigDecimal("129.99"))
                .build();

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(999L, updateDTO);
        });

        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_WithValidId_ShouldDeleteProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteProduct_WithInvalidId_ShouldThrowException() {
        when(productRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            productService.deleteProduct(999L);
        });

        verify(productRepository, times(1)).existsById(999L);
        verify(productRepository, never()).deleteById(any());
    }
}