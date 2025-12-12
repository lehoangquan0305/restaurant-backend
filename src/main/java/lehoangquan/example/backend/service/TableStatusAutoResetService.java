package lehoangquan.example.backend.service;

import lehoangquan.example.backend.model.DiningTable;
import lehoangquan.example.backend.repository.DiningTableRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TableStatusAutoResetService {
    private final DiningTableRepository tableRepository;

    public TableStatusAutoResetService(DiningTableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    // minutes after which an OCCUPIED table becomes AVAILABLE (default 45)
    @Value("${tables.reset.minutes:45}")
    private long resetMinutes;

    // check interval (ms) - default every 5 minutes
    @Value("${tables.reset.check-ms:300000}")
    private long checkMs;

    // Runs periodically and resets tables that have been OCCUPIED longer than resetMinutes
    @Scheduled(fixedDelayString = "${tables.reset.check-ms:300000}")
    public void resetOccupiedTables() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusMinutes(resetMinutes);
            List<DiningTable> toReset = tableRepository.findAll().stream()
                    .filter(t -> t.getStatus() == DiningTable.TableStatus.OCCUPIED)
                    .filter(t -> t.getStatusChangedAt() != null && t.getStatusChangedAt().isBefore(cutoff))
                    .collect(Collectors.toList());

            if (!toReset.isEmpty()) {
                toReset.forEach(t -> {
                    t.setStatus(DiningTable.TableStatus.AVAILABLE);
                    t.setStatusChangedAt(LocalDateTime.now());
                });
                tableRepository.saveAll(toReset);
            }
        } catch (Exception e) {
            // log but don't fail scheduling
            System.err.println("Error in TableStatusAutoResetService: " + e.getMessage());
        }
    }
}
