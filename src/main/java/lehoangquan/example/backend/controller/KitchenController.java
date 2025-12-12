package lehoangquan.example.backend.controller;

import lehoangquan.example.backend.model.OrderItem;
import lehoangquan.example.backend.repository.OrderRepository;
import lehoangquan.example.backend.dto.KitchenItemDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kitchen")
public class KitchenController {

    private final OrderRepository orderRepository;

    public KitchenController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/items/pending")
    public List<KitchenItemDTO> listPendingItems() {
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == lehoangquan.example.backend.model.OrderEntity.Status.NEW || 
                           o.getStatus() == lehoangquan.example.backend.model.OrderEntity.Status.IN_PROGRESS)
                .flatMap(o -> o.getItems().stream())
                .filter(i -> i.getStatus() != OrderItem.ItemStatus.CANCELLED)
                .map(KitchenItemDTO::fromOrderItem)
                .toList();
    }

    @PutMapping("/items/{orderId}/{itemId}/status")
    public ResponseEntity<?> updateItemStatus(@PathVariable Long orderId, @PathVariable Long itemId, @RequestParam("status") OrderItem.ItemStatus status) {
        return orderRepository.findById(orderId).map(order -> {
            order.getItems().stream().filter(i -> i.getId().equals(itemId)).findFirst().ifPresent(i -> i.setStatus(status));
            orderRepository.save(order);
            return ResponseEntity.ok().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
