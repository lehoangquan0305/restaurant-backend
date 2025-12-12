package lehoangquan.example.backend.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateDTO {
    private Long tableId;
    private Long reservationId;
    private String notes;
    private String status;
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        private Long menuItemId;
        private Integer quantity;
        private BigDecimal price;
    }
}
