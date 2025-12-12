package lehoangquan.example.backend.dto;

import lehoangquan.example.backend.model.OrderItem;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenItemDTO {
    private Long id;
    private Long orderId;
    private String menuItemName;
    private Integer quantity;
    private BigDecimal price;
    private String status;

    public static KitchenItemDTO fromOrderItem(OrderItem item) {
        return KitchenItemDTO.builder()
                .id(item.getId())
                .orderId(item.getOrder().getId())
                .menuItemName(item.getMenuItem() != null ? item.getMenuItem().getName() : null)
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .status(item.getStatus().toString())
                .build();
    }
}
