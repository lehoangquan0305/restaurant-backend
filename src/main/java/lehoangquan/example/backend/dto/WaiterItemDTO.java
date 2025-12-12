package lehoangquan.example.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaiterItemDTO {
    private Long itemId;
    private String menuItemName;
    private Integer quantity;
    private String itemStatus;  // PENDING, COOKING, DONE, CANCELLED
}
