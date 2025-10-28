package order_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import order_service.application.controller.OrderController;
import order_service.application.service.OrderService;
import order_service.domain.model.Order;
import order_service.domain.model.OrderProduct;
import order_service.domain.model.OrderStatus;
import order_service.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    private static final String TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6ImFkbWluMTIzIiwicm9sZSI6IkFETUlOIiwiaWQiOjYsImVtYWlsIjoiYWRtaW5AeWFob28uY29tIiwiaWF0IjoxNzQxMjgxNjQzLCJleHAiOjc3OTM0MTI4MTY0M30.TqoXXgcmquK4PwnEjnLYRs-sdwynTGUQz4PgqdIs5Mws";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtUtil jwtUtil;


    @Test
    void getOrderById_Found() throws Exception {
        Order order = new Order();
        order.setId(2L);
        order.setClientId(456L);
        order.setStatus(OrderStatus.CREATED);

        when(orderService.getOrderById(2L)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/orders/2")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.clientId").value(456L))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void completeOrder_ExpectOk() throws Exception {
        Order completed = new Order();
        completed.setId(3L);
        completed.setClientId(789L);
        completed.setStatus(OrderStatus.COMPLETED);

        when(orderService.markOrderAsCompleted(3L, TOKEN)).thenReturn(completed);

        mockMvc.perform(put("/orders/3/complete")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void getOrderById_NotFound() throws Exception {
        when(orderService.getOrderById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/9999999999")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_Invalid() throws Exception {
        mockMvc.perform(put("/orders/7/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"INVALID_STATUS\""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteOrder_Success() throws Exception {
        when(orderService.deleteOrder(10L)).thenReturn(true);

        mockMvc.perform(delete("/orders/10")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteOrder_NotFound() throws Exception {
        when(orderService.deleteOrder(11L)).thenReturn(false);

        mockMvc.perform(delete("/orders/11")
                        .header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void toggleStatus_FromCreatedToCompleted() throws Exception {
        Order existing = new Order();
        existing.setId(15L);
        existing.setStatus(OrderStatus.CREATED);

        Order updated = new Order();
        updated.setId(15L);
        updated.setStatus(OrderStatus.COMPLETED);

        when(orderService.getOrderById(15L)).thenReturn(Optional.of(existing));
        when(orderService.markOrderAsCompleted(15L, TOKEN)).thenReturn(updated);

        mockMvc.perform(put("/orders/15/toggle")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void toggleStatus_OrderNotFound() throws Exception {
        when(orderService.getOrderById(100L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/orders/100/toggle")
                        .header("Authorization", TOKEN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelWithEmail_Success() throws Exception {
        Order cancelled = new Order();
        cancelled.setId(20L);
        cancelled.setStatus(OrderStatus.CANCELLED);

        when(orderService.cancelOrderWithEmail(20L, TOKEN)).thenReturn(cancelled);

        mockMvc.perform(put("/orders/20/cancel-with-email")
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}


