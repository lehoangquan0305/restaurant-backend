package lehoangquan.example.backend.repository;

import lehoangquan.example.backend.model.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {
    Optional<DiningTable> findByName(String name);
}
