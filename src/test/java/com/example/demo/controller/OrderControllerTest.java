package com.example.demo.controller;

import com.example.demo.dto.OrderDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderDTO orderDTO;
    private List<OrderDTO> orderDTOList;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @BeforeEach
    void setUp() {
        OrderDTO.OrderItemDTO orderItemDTO = OrderDTO.OrderItemDTO.builder()
                .id(1L)
                .productId(1L)
                .productName("Test Product")
                .price(new BigDecimal("99.99"))
                .quantity(2)
                .build();

        orderDTO = OrderDTO.builder()
                .id(1L)
                .buyerEmail("test@example.com")
                .orderTime(LocalDateTime.now())
                .totalValue(new BigDecimal("199.98"))
                .items(Collections.singletonList(orderItemDTO))
                .build();

        OrderDTO orderDTO2 = OrderDTO.builder()
                .id(2L)
                .buyerEmail("another@example.com")
                .orderTime(LocalDateTime.now().minusDays(1))
                .totalValue(new BigDecimal("149.97"))
                .items(Collections.singletonList(orderItemDTO))
                .build();

        orderDTOList = Arrays.asList(orderDTO, orderDTO2);

        startDate = LocalDateTime.now().minusDays(7);
        endDate = LocalDateTime.now();
    }

    @Test
    void getAllOrders_ShouldReturnOrdersList() throws Exception {
        when(orderService.getAllOrders()).thenReturn(orderDTOList);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].buyerEmail", is("test@example.com")))
                .andExpect(jsonPath("$[0].totalValue", is(199.98)))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].buyerEmail", is("another@example.com")))
                .andExpect(jsonPath("$[1].totalValue", is(149.97)));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void getOrderById_WithValidId_ShouldReturnOrder() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(orderDTO);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.buyerEmail", is("test@example.com")))
                .andExpect(jsonPath("$.totalValue", is(199.98)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productName", is("Test Product")));

        verify(orderService, times(1)).getOrderById(1L);
    }

    @Test
    void getOrderById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        when(orderService.getOrderById(999L)).thenThrow(new ResourceNotFoundException("Order not found with id: 999"));

        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).getOrderById(999L);
    }

    @Test
    void getOrdersByDateRange_ShouldReturnOrdersList() throws Exception {
        when(orderService.getOrdersBetweenDates(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(orderDTOList);

        mockMvc.perform(get("/api/orders/byDateRange")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(orderService, times(1)).getOrdersBetweenDates(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void placeOrder_WithValidData_ShouldReturnCreatedOrder() throws Exception {
        OrderDTO.OrderItemDTO inputOrderItemDTO = OrderDTO.OrderItemDTO.builder()
                .productId(1L)
                .quantity(3)
                .build();

        OrderDTO inputOrderDTO = OrderDTO.builder()
                .buyerEmail("new@example.com")
                .items(Collections.singletonList(inputOrderItemDTO))
                .build();

        when(orderService.createOrder(any(OrderDTO.class))).thenReturn(orderDTO);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputOrderDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.buyerEmail", is("test@example.com")))
                .andExpect(jsonPath("$.totalValue", is(199.98)));

        verify(orderService, times(1)).createOrder(any(OrderDTO.class));
    }

    @Test
    void placeOrder_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        OrderDTO invalidOrderDTO = OrderDTO.builder()
                .buyerEmail("invalid-email")  // Invalid email format
                .items(Collections.emptyList())  // Empty items list
                .build();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOrderDTO)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(OrderDTO.class));
    }
}
