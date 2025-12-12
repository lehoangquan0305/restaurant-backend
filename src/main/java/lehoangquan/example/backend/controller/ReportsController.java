package lehoangquan.example.backend.controller;

import lehoangquan.example.backend.model.OrderEntity;
import lehoangquan.example.backend.repository.OrderRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    private final OrderRepository orderRepository;

    public ReportsController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/summary/today")
    public Map<String, Object> summaryToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(today, LocalTime.MAX);

        var orders = orderRepository.findAll().stream().filter(o -> o.getCreatedAt().isAfter(start) && o.getCreatedAt().isBefore(end)).toList();
        long totalOrders = orders.size();
        BigDecimal revenue = orders.stream().map(o -> o.getTotal() == null ? BigDecimal.ZERO : o.getTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
        long inProgress = orders.stream().filter(o -> o.getStatus() == OrderEntity.Status.IN_PROGRESS).count();
        long completed = orders.stream().filter(o -> o.getStatus() == OrderEntity.Status.COMPLETED).count();

        return Map.of(
                "totalOrders", totalOrders,
                "revenue", revenue,
                "inProgress", inProgress,
                "completed", completed
        );
    }
}
