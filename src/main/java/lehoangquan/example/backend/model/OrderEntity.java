package lehoangquan.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.FetchType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
@Entity
@Table(name = "orders")
@Data
@ToString(exclude = {"items", "table", "reservation"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "table_id")
    private DiningTable table;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @JsonBackReference
    private Customer createdBy;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    private String notes;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.NEW;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {NEW, IN_PROGRESS, COMPLETED, CANCELLED}

    @JsonProperty("customerName")
    public String getCustomerName() {
        return this.createdBy != null ? this.createdBy.getFullName() : null;
    }
}
