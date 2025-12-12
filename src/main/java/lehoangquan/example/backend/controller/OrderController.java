package lehoangquan.example.backend.controller;

import lehoangquan.example.backend.model.OrderEntity;
import lehoangquan.example.backend.dto.OrderCreateDTO;
import lehoangquan.example.backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderEntity> list() {
        return orderService.listForCurrentUser();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody OrderCreateDTO dto) {
        try {
            System.out.println("=== OrderController.create ===");
            System.out.println("DTO received: " + dto);
            OrderEntity result = orderService.createFromDTO(dto);
            System.out.println("Order created with ID: " + result.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("Error creating order: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderEntity> get(@PathVariable Long id) {
        return orderService.listAll().stream().filter(o -> o.getId().equals(id)).findFirst()
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

