package lehoangquan.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@Data
@ToString(exclude = {"order", "menuItem"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    @Builder.Default
    private Integer quantity = 1;

    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private OrderEntity order;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ItemStatus status = ItemStatus.PENDING;

    public enum ItemStatus {PENDING, COOKING, DONE, CANCELLED}
}
