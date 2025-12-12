package lehoangquan.example.backend.repository;

import lehoangquan.example.backend.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import lehoangquan.example.backend.model.Customer;
import lehoangquan.example.backend.model.Reservation;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
	List<OrderEntity> findByCreatedBy(Customer customer);

	// Find orders associated with a reservation
	List<OrderEntity> findByReservation(Reservation reservation);
	
	// Eager load items to avoid lazy loading issues
	@Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.items WHERE o.id = ?1")
	Optional<OrderEntity> findByIdWithItems(Long id);
}
