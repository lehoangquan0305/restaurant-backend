package lehoangquan.example.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "reservation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    private String customerPhone;

    private Integer partySize;

    // FIX: nhận từ FE dạng String để không lỗi JSON parse
    private String reservationTime;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.PENDING;

    @ManyToOne
    @JoinColumn(name = "table_id")
    private DiningTable table;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {PENDING, CONFIRMED, CANCELLED, COMPLETED}
}

