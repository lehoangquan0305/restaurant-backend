package lehoangquan.example.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    private BigDecimal amount;

    @Builder.Default
    private boolean paid = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
