package lehoangquan.example.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dining_table")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiningTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "Bàn 1"

    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TableStatus status = TableStatus.AVAILABLE;

    @Builder.Default
    private java.time.LocalDateTime statusChangedAt = java.time.LocalDateTime.now();

    public enum TableStatus {
        AVAILABLE, RESERVED, OCCUPIED, OUT_OF_SERVICE
    }
}
