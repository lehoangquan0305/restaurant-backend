package lehoangquan.example.backend.controller;

import lehoangquan.example.backend.model.DiningTable;
import lehoangquan.example.backend.repository.DiningTableRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class DiningTableController {

    private final DiningTableRepository tableRepository;

    public DiningTableController(DiningTableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    @GetMapping
    public List<DiningTable> list() {
        return tableRepository.findAll();
    }

    @PostMapping
    public DiningTable create(@RequestBody DiningTable table) {
        return tableRepository.save(table);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiningTable> update(@PathVariable Long id, @RequestBody DiningTable req) {
        return tableRepository.findById(id).map(t -> {
            t.setName(req.getName());
            t.setCapacity(req.getCapacity());
            t.setStatus(req.getStatus());
            t.setStatusChangedAt(java.time.LocalDateTime.now());
            tableRepository.save(t);
            return ResponseEntity.ok(t);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (tableRepository.existsById(id)) {
            tableRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
