package lehoangquan.example.backend.controller;
import lehoangquan.example.backend.model.DiningTable;
import lehoangquan.example.backend.model.Reservation;
import lehoangquan.example.backend.repository.DiningTableRepository;
import lehoangquan.example.backend.repository.ReservationRepository;
import lehoangquan.example.backend.websocket.OrderWebSocketController;
import lehoangquan.example.backend.repository.OrderRepository;
import lehoangquan.example.backend.repository.InvoiceRepository;
import lehoangquan.example.backend.repository.PaymentRepository;
import lehoangquan.example.backend.model.OrderEntity;
import lehoangquan.example.backend.model.Invoice;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final DiningTableRepository tableRepository;
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final OrderWebSocketController ws;

    public ReservationController(ReservationRepository reservationRepository, DiningTableRepository tableRepository, OrderRepository orderRepository, InvoiceRepository invoiceRepository, PaymentRepository paymentRepository, OrderWebSocketController ws) {
        this.reservationRepository = reservationRepository;
        this.tableRepository = tableRepository;
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.ws = ws;
    }

    @GetMapping
    public List<Reservation> list() {
        return reservationRepository.findAll();
    }

    @PostMapping
public Reservation create(@RequestBody Map<String, Object> payload) {
    if (payload == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload is null");
    }

    String customerName = (String) payload.get("customerName");
    String customerPhone = (String) payload.get("customerPhone");
    if (customerPhone == null || !customerPhone.matches("\\d{9,11}")) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số điện thoại không hợp lệ (9–11 số)");
}
    Integer partySize = (payload.get("partySize") != null) ? ((Number) payload.get("partySize")).intValue() : 1;
    String reservationTime = (String) payload.get("reservationTime");
    String statusStr = (String) payload.getOrDefault("status", "PENDING");
    Long tableId = payload.get("tableId") != null ? Long.valueOf(payload.get("tableId").toString()) : null;

    Reservation r = new Reservation();
    r.setCustomerName(customerName);
    r.setCustomerPhone(customerPhone);
    r.setPartySize(partySize);
    r.setReservationTime(reservationTime);
    r.setStatus(Reservation.Status.valueOf(statusStr));

    if (tableId != null) {
        DiningTable table = tableRepository.findById(tableId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Table not found"));
        if (partySize > table.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected table capacity is less than party size");
        }
        table.setStatus(DiningTable.TableStatus.RESERVED);
        tableRepository.save(table);
        r.setTable(table);
    }

    Reservation saved = reservationRepository.save(r);
    ws.broadcastOrderUpdate(saved); // placeholder
    return saved;
}



    @PutMapping("/{id}")
public ResponseEntity<Reservation> update(@PathVariable Long id, @RequestBody Reservation req) {
    return reservationRepository.findById(id).map(r -> {
        r.setCustomerName(req.getCustomerName());
        r.setCustomerPhone(req.getCustomerPhone());
        r.setPartySize(req.getPartySize());
        r.setReservationTime(req.getReservationTime());

        DiningTable oldTable = r.getTable();
        DiningTable newTable = null;

        if (req.getTable() != null && req.getTable().getId() != null) {
            newTable = tableRepository.findById(req.getTable().getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Table not found"));

            // Validate capacity
            if (req.getPartySize() != null && newTable.getCapacity() != null && req.getPartySize() > newTable.getCapacity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected table capacity is less than party size");
            }

            // Check if table is available or is the same as current
            if (newTable.getStatus() != DiningTable.TableStatus.AVAILABLE && (oldTable == null || !newTable.getId().equals(oldTable.getId()))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bàn đã được đặt hoặc đang sử dụng, vui lòng chọn bàn khác");
            }

            // Release old table if changed
            if (oldTable != null && !oldTable.getId().equals(newTable.getId())) {
                oldTable.setStatus(DiningTable.TableStatus.AVAILABLE);
                tableRepository.save(oldTable);
            }

            // Set new table as reserved
            newTable.setStatus(DiningTable.TableStatus.RESERVED);
            tableRepository.save(newTable);

            r.setTable(newTable);
        }

        r.setStatus(req.getStatus());
        Reservation saved = reservationRepository.save(r);

        ws.broadcastOrderUpdate(saved);
        return ResponseEntity.ok(saved);
    }).orElseGet(() -> ResponseEntity.notFound().build());
}


    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return reservationRepository.findById(id).map(r -> {
            System.out.println("=== Deleting reservation: " + id);
            
            // Tìm tất cả orders liên quan đến reservation này
            java.util.List<OrderEntity> orders = orderRepository.findByReservation(r);
            
            if (orders != null && !orders.isEmpty()) {
                System.out.println("Found " + orders.size() + " orders to delete");
                
                // Xóa tất cả payments và invoices của mỗi order
                for (OrderEntity order : orders) {
                    System.out.println("Processing order: " + order.getId());
                    
                    // Tìm invoices của order này
                    java.util.Optional<Invoice> invoiceOpt = invoiceRepository.findByOrderId(order.getId());
                    if (invoiceOpt.isPresent()) {
                        Invoice invoice = invoiceOpt.get();
                        System.out.println("Found invoice: " + invoice.getId());
                        
                        // Xóa tất cả payments của invoice này
                        java.util.List<lehoangquan.example.backend.model.Payment> payments = paymentRepository.findByInvoiceId(invoice.getId());
                        if (payments != null && !payments.isEmpty()) {
                            System.out.println("Deleting " + payments.size() + " payments");
                            paymentRepository.deleteAll(payments);
                        }
                        
                        // Xóa invoice
                        System.out.println("Deleting invoice: " + invoice.getId());
                        invoiceRepository.delete(invoice);
                    }
                    
                    // Xóa order items (cascade sẽ tự động xóa)
                    // Xóa order
                    System.out.println("Deleting order: " + order.getId());
                    orderRepository.delete(order);
                }
            }

            // Release table if reservation is cancelled
            if (r.getTable() != null) {
                DiningTable table = r.getTable();
                table.setStatus(DiningTable.TableStatus.AVAILABLE);
                tableRepository.save(table);
            }

            // Xóa reservation
            System.out.println("Deleting reservation: " + id);
            reservationRepository.deleteById(id);
            return ResponseEntity.ok().<Void>build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
