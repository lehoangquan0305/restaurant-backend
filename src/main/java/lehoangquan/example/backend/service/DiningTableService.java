package lehoangquan.example.backend.service;

import lehoangquan.example.backend.model.DiningTable;
import lehoangquan.example.backend.repository.DiningTableRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiningTableService {
    private final DiningTableRepository repository;

    public DiningTableService(DiningTableRepository repository) {
        this.repository = repository;
    }

    public List<DiningTable> listAll() {
        return repository.findAll();
    }

    public DiningTable create(DiningTable table) {
        return repository.save(table);
    }

    public DiningTable update(Long id, DiningTable req) {
        return repository.findById(id).map(t -> {
            t.setName(req.getName());
            t.setCapacity(req.getCapacity());
            t.setStatus(req.getStatus());
            return repository.save(t);
        }).orElse(null);
    }
}
