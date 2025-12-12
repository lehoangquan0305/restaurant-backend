package lehoangquan.example.backend.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaiterTableDTO {
    private Long tableId;
    private String tableName;
    private Integer capacity;
    private Long orderId;
    private List<WaiterItemDTO> items;
    private String notes;
    private Boolean allItemsDone;  // true nếu tất cả items đều DONE
}
