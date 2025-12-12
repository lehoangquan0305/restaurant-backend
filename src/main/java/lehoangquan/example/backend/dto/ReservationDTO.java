package lehoangquan.example.backend.dto;

import lombok.Data;

@Data
public class ReservationDTO {
    private String customerName;
    private String customerPhone;
    private Integer partySize;
    private String reservationTime; // dạng string từ FE
    private Long tableId; // FE chỉ gửi id bàn
    private String status; // optional, default CONFIRMED
}
