package lehoangquan.example.backend.service;

import lehoangquan.example.backend.dto.WaiterTableDTO;
import lehoangquan.example.backend.dto.WaiterItemDTO;
import lehoangquan.example.backend.model.DiningTable;
import lehoangquan.example.backend.model.OrderEntity;
import lehoangquan.example.backend.model.OrderItem;
import lehoangquan.example.backend.repository.DiningTableRepository;
import lehoangquan.example.backend.repository.OrderRepository;
import lehoangquan.example.backend.repository.ReservationRepository;
import lehoangquan.example.backend.model.Reservation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WaiterService {
        private final DiningTableRepository tableRepository;
        private final OrderRepository orderRepository;
        private final ReservationRepository reservationRepository;

        public WaiterService(DiningTableRepository tableRepository, OrderRepository orderRepository, ReservationRepository reservationRepository) {
                this.tableRepository = tableRepository;
                this.orderRepository = orderRepository;
                this.reservationRepository = reservationRepository;
        }

    /**
     * Lấy danh sách bàn RESERVED với chi tiết đơn hàng
     */
    public List<WaiterTableDTO> getReservedTablesWithOrders() {
        return tableRepository.findAll().stream()
                .filter(t -> t.getStatus() == DiningTable.TableStatus.RESERVED)
                .map(this::mapToWaiterTableDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map DiningTable -> WaiterTableDTO với chi tiết orders
     */
    private WaiterTableDTO mapToWaiterTableDTO(DiningTable table) {
        // Tìm order NEW của bàn này
        OrderEntity order = orderRepository.findAll().stream()
                .filter(o -> o.getTable() != null && o.getTable().getId().equals(table.getId())
                        && o.getStatus() == OrderEntity.Status.NEW)
                .findFirst()
                .orElse(null);

        WaiterTableDTO dto = new WaiterTableDTO();
        dto.setTableId(table.getId());
        dto.setTableName(table.getName());
        dto.setCapacity(table.getCapacity());
        dto.setNotes(order != null ? order.getNotes() : "");
        
        if (order != null) {
            dto.setOrderId(order.getId());
            
            // Map items
            List<WaiterItemDTO> items = order.getItems().stream()
                    .map(item -> new WaiterItemDTO(
                            item.getId(),
                            item.getMenuItem() != null ? item.getMenuItem().getName() : "Unknown",
                            item.getQuantity(),
                            item.getStatus().toString()
                    ))
                    .collect(Collectors.toList());
            dto.setItems(items);
            
            // Kiểm tra tất cả items đều DONE
            boolean allDone = order.getItems().stream()
                    .allMatch(item -> item.getStatus() == OrderItem.ItemStatus.DONE);
            dto.setAllItemsDone(allDone);
        } else {
            dto.setItems(List.of());
            dto.setAllItemsDone(false);
        }
        
        return dto;
    }

    /**
     * Phục vụ bàn: chuyển bàn -> OCCUPIED, order -> COMPLETED
     */
    public void serveTable(Long tableId) {
        DiningTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found"));

        // Tìm order NEW của bàn này
        OrderEntity order = orderRepository.findAll().stream()
                .filter(o -> o.getTable() != null && o.getTable().getId().equals(tableId)
                        && o.getStatus() == OrderEntity.Status.NEW)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active order for this table"));

        // Kiểm tra tất cả items đều DONE
        boolean allDone = order.getItems().stream()
                .allMatch(item -> item.getStatus() == OrderItem.ItemStatus.DONE);
        if (!allDone) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not all items are done cooking yet");
        }

        // Chuyển bàn thành OCCUPIED và cập nhật timestamp
        table.setStatus(DiningTable.TableStatus.OCCUPIED);
        table.setStatusChangedAt(java.time.LocalDateTime.now());
        tableRepository.save(table);

                // Chuyển order thành COMPLETED (không còn NEW)
                order.setStatus(OrderEntity.Status.COMPLETED);
                orderRepository.save(order);

                // Nếu order liên kết tới reservation thì đánh dấu reservation là COMPLETED
                if (order.getReservation() != null) {
                        Reservation res = order.getReservation();
                        try {
                                res.setStatus(Reservation.Status.COMPLETED);
                                reservationRepository.save(res);
                        } catch (Exception e) {
                                // Do not block serving if reservation update fails; log and continue
                                System.err.println("Failed to update reservation status to COMPLETED: " + e.getMessage());
                        }
                }
    }
}
