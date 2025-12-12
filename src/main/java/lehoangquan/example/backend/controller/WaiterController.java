package lehoangquan.example.backend.controller;

import lehoangquan.example.backend.dto.WaiterTableDTO;
import lehoangquan.example.backend.service.WaiterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/waiter")
public class WaiterController {
    private final WaiterService waiterService;

    public WaiterController(WaiterService waiterService) {
        this.waiterService = waiterService;
    }

    /**
     * Lấy danh sách bàn RESERVED với chi tiết đơn hàng
     */
    @GetMapping("/tables")
    @PreAuthorize("hasAnyRole('WAITER', 'ADMIN')")
    public ResponseEntity<List<WaiterTableDTO>> getReservedTables() {
        return ResponseEntity.ok(waiterService.getReservedTablesWithOrders());
    }

    /**
     * Phục vụ bàn: chuyển bàn -> OCCUPIED, order -> COMPLETED
     */
    @PostMapping("/tables/{tableId}/serve")
    @PreAuthorize("hasAnyRole('WAITER', 'ADMIN')")
    public ResponseEntity<String> serveTable(@PathVariable Long tableId) {
        waiterService.serveTable(tableId);
        return ResponseEntity.ok("Table " + tableId + " served successfully");
    }
}
