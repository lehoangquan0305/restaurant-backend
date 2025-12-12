package lehoangquan.example.backend.service;

import lehoangquan.example.backend.dto.ReservationDTO;
import lehoangquan.example.backend.model.DiningTable;
import lehoangquan.example.backend.model.Reservation;
import lehoangquan.example.backend.repository.DiningTableRepository;
import lehoangquan.example.backend.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final DiningTableRepository tableRepository;

    public ReservationService(ReservationRepository reservationRepository, DiningTableRepository tableRepository) {
        this.reservationRepository = reservationRepository;
        this.tableRepository = tableRepository;
    }

    public List<Reservation> listAll() {
        return reservationRepository.findAll();
    }
    public Reservation createFromDTO(ReservationDTO dto) {
    DiningTable table = null;
    if (dto.getTableId() != null) {
        table = tableRepository.findById(dto.getTableId())
                .orElseThrow(() -> new RuntimeException("Table not found"));
        table.setStatus(DiningTable.TableStatus.RESERVED);
        tableRepository.save(table);
    }

    Reservation r = Reservation.builder()
            .customerName(dto.getCustomerName())
            .customerPhone(dto.getCustomerPhone())
            .partySize(dto.getPartySize())
            .reservationTime(dto.getReservationTime())
            .status(dto.getStatus() != null ? Reservation.Status.valueOf(dto.getStatus()) : Reservation.Status.CONFIRMED)
            .table(table)
            .build();

    return reservationRepository.save(r);
}


    public Reservation create(Reservation r) {
        if (r.getTable() != null && r.getTable().getId() != null) {
            DiningTable table = tableRepository.findById(r.getTable().getId()).orElse(null);
            if (table != null) {
                table.setStatus(DiningTable.TableStatus.RESERVED);
                tableRepository.save(table);
                r.setTable(table);
            }
        }
        return reservationRepository.save(r);
    }

    public Reservation update(Long id, Reservation req) {
        return reservationRepository.findById(id).map(r -> {
            r.setCustomerName(req.getCustomerName());
            r.setCustomerPhone(req.getCustomerPhone());
            r.setPartySize(req.getPartySize());
            r.setReservationTime(req.getReservationTime());
            r.setStatus(req.getStatus());
            return reservationRepository.save(r);
        }).orElse(null);
    }
}
