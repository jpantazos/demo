package com.example.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.dto.ProductDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDTO productDTO;
    private List<ProductDTO> productDTOList;

    @BeforeEach
    void setUp() {
        productDTO = ProductDTO.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .build();

        ProductDTO productDTO2 = ProductDTO.builder()
                .id(2L)
                .name("Another Product")
                .price(new BigDecimal("49.99"))
                .build();

        productDTOList = Arrays.asList(productDTO, productDTO2);
    }

    @Test
    void getAllProducts_ShouldReturnProductsList() throws Exception {
        when(productService.getAllProducts()).thenReturn(productDTOList);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Product")))
                .andExpect(jsonPath("$[0].price", is(99.99)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Another Product")))
                .andExpect(jsonPath("$[1].price", is(49.99)));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void getProductById_WithValidId_ShouldReturnProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productDTO);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Product")))
                .andExpect(jsonPath("$.price", is(99.99)));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    void getProductById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        when(productService.getProductById(999L)).thenThrow(new ResourceNotFoundException("Product not found with id: 999"));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getProductById(999L);
    }

    @Test
    void createProduct_WithValidData_ShouldReturnCreatedProduct() throws Exception {
        ProductDTO inputProductDTO = ProductDTO.builder()
                .name("New Product")
                .price(new BigDecimal("79.99"))
                .build();

        ProductDTO createdProductDTO = ProductDTO.builder()
                .id(3L)
                .name("New Product")
                .price(new BigDecimal("79.99"))
                .build();

        when(productService.createProduct(any(ProductDTO.class))).thenReturn(createdProductDTO);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputProductDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("New Product")))
                .andExpect(jsonPath("$.price", is(79.99)));

        verify(productService, times(1)).createProduct(any(ProductDTO.class));
    }

    @Test
    void createProduct_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        ProductDTO invalidProductDTO = ProductDTO.builder()
                .name("")  // Invalid: empty name
                .price(new BigDecimal("-1.0"))  // Invalid: negative price
                .build();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProductDTO)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any(ProductDTO.class));
    }

    @Test
    void updateProduct_WithValidData_ShouldReturnUpdatedProduct() throws Exception {
        ProductDTO updateProductDTO = ProductDTO.builder()
                .name("Updated Product")
                .price(new BigDecimal("129.99"))
                .build();

        ProductDTO updatedProductDTO = ProductDTO.builder()
                .id(1L)
                .name("Updated Product")
                .price(new BigDecimal("129.99"))
                .build();

        when(productService.updateProduct(eq(1L), any(ProductDTO.class))).thenReturn(updatedProductDTO);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProductDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Updated Product")))
                .andExpect(jsonPath("$.price", is(129.99)));

        verify(productService, times(1)).updateProduct(eq(1L), any(ProductDTO.class));
    }

    @Test
    void deleteProduct_WithValidId_ShouldReturnNoContent() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }
}
