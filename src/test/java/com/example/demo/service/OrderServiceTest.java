package com.example.demo.service;

import com.example.demo.domain.Order;
import com.example.demo.domain.OrderItem;
import com.example.demo.domain.Product;
import com.example.demo.dto.OrderDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Order order1;
    private Order order2;
    private Product product;
    private List<Order> orderList;
    private OrderDTO orderDTO;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .productId(1L)
                .productName("Test Product")
                .price(new BigDecimal("99.99"))
                .quantity(2)
                .build();

        order1 = new Order();
        order1.setId(1L);
        order1.setBuyerEmail("test@example.com");
        order1.setOrderTime(LocalDateTime.now());
        order1.addItem(orderItem);
        order1.calculateTotalValue();

        order2 = new Order();
        order2.setId(2L);
        order2.setBuyerEmail("another@example.com");
        order2.setOrderTime(LocalDateTime.now().minusDays(1));
        order2.addItem(orderItem);
        order2.calculateTotalValue();

        orderList = Arrays.asList(order1, order2);

        OrderDTO.OrderItemDTO orderItemDTO = OrderDTO.OrderItemDTO.builder()
                .productId(1L)
                .quantity(3)
                .build();

        orderDTO = OrderDTO.builder()
                .buyerEmail("new@example.com")
                .items(Collections.singletonList(orderItemDTO))
                .build();

        startDate = LocalDateTime.now().minusDays(7);
        endDate = LocalDateTime.now();
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        when(orderRepository.findAll()).thenReturn(orderList);

        List<OrderDTO> result = orderService.getAllOrders();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getBuyerEmail()).isEqualTo("test@example.com");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getBuyerEmail()).isEqualTo("another@example.com");

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getOrderById_WithValidId_ShouldReturnOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order1));

        OrderDTO result = orderService.getOrderById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBuyerEmail()).isEqualTo("test@example.com");
        assertThat(result.getItems()).hasSize(1);

        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void getOrderById_WithInvalidId_ShouldThrowException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderById(999L);
        });

        verify(orderRepository, times(1)).findById(999L);
    }

    @Test
    void getOrdersBetweenDates_ShouldReturnFilteredOrders() {
        when(orderRepository.findAllOrdersBetweenDates(startDate, endDate)).thenReturn(orderList);

        List<OrderDTO> result = orderService.getOrdersBetweenDates(startDate, endDate);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);

        verify(orderRepository, times(1)).findAllOrdersBetweenDates(startDate, endDate);
    }

    @Test
    void createOrder_ShouldReturnCreatedOrder() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(3L);
            return savedOrder;
        });

        OrderDTO result = orderService.createOrder(orderDTO);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getBuyerEmail()).isEqualTo("new@example.com");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getProductName()).isEqualTo("Test Product");

        verify(productRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_WithInvalidProductId_ShouldThrowException() {
        OrderDTO.OrderItemDTO orderItemDTO = OrderDTO.OrderItemDTO.builder()
                .productId(999L)
                .quantity(3)
                .build();

        OrderDTO invalidOrderDTO = OrderDTO.builder()
                .buyerEmail("new@example.com")
                .items(Collections.singletonList(orderItemDTO))
                .build();

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.createOrder(invalidOrderDTO);
        });

        verify(productRepository, times(1)).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }
}
