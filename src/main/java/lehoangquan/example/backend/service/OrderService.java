package lehoangquan.example.backend.service;

import lehoangquan.example.backend.model.OrderEntity;
import lehoangquan.example.backend.model.OrderItem;
import lehoangquan.example.backend.model.MenuItem;
import lehoangquan.example.backend.model.DiningTable;
import lehoangquan.example.backend.model.Reservation;
import lehoangquan.example.backend.dto.OrderCreateDTO;
import lehoangquan.example.backend.repository.OrderRepository;
import lehoangquan.example.backend.repository.MenuItemRepository;
import lehoangquan.example.backend.repository.DiningTableRepository;
import lehoangquan.example.backend.repository.ReservationRepository;
import lehoangquan.example.backend.repository.CustomerRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final DiningTableRepository tableRepository;
    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final lehoangquan.example.backend.websocket.OrderWebSocketController ws;

    public OrderService(OrderRepository orderRepository, MenuItemRepository menuItemRepository, 
                       DiningTableRepository tableRepository, ReservationRepository reservationRepository, CustomerRepository customerRepository, lehoangquan.example.backend.websocket.OrderWebSocketController ws) {
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
        this.tableRepository = tableRepository;
        this.reservationRepository = reservationRepository;
        this.customerRepository = customerRepository;
        this.ws = ws;
    }

    public List<OrderEntity> listAll() {
        return orderRepository.findAll();
    }

    public List<OrderEntity> listForCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return List.of();
            boolean isEmployee = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().startsWith("ROLE_EMPLOYEE") || a.getAuthority().startsWith("ROLE_ADMIN"));
            if (isEmployee) {
                return orderRepository.findAll();
            }
            String username = auth.getName();
            return customerRepository.findByUsername(username)
                    .map(orderRepository::findByCreatedBy)
                    .orElse(List.of());
        } catch (Exception e) {
            return List.of();
        }
    }

    public OrderEntity create(OrderEntity order) {
        // compute total
        BigDecimal total = BigDecimal.ZERO;
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            total = order.getItems().stream()
                    .filter(i -> i.getPrice() != null)
                    .map(i -> i.getPrice().multiply(java.math.BigDecimal.valueOf(i.getQuantity() != null ? i.getQuantity() : 1)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        order.setTotal(total);
        
        // Set bidirectional relationship
        if (order.getItems() != null) {
            order.getItems().forEach(it -> it.setOrder(order));
        }
        
        OrderEntity saved = orderRepository.save(order);
        try { ws.broadcastOrderUpdate(saved); } catch (Exception ignored) {}
        return saved;
    }

    public OrderEntity createFromDTO(OrderCreateDTO dto) {
        System.out.println("=== OrderService.createFromDTO ===");
        System.out.println("DTO: " + dto);
        System.out.println("DTO.getTableId(): " + (dto != null ? dto.getTableId() : "null"));
        System.out.println("DTO.getItems(): " + (dto != null && dto.getItems() != null ? dto.getItems().size() : "null"));
        
        if (dto == null || dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must have at least one item");
        }

        // Load table
        DiningTable table = null;
        if (dto.getTableId() != null) {
            System.out.println("Loading table with ID: " + dto.getTableId());
            table = tableRepository.findById(dto.getTableId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Table not found"));
            System.out.println("Table loaded: " + table);
            
            // Update table status to RESERVED when order is created
            if (table.getStatus() == DiningTable.TableStatus.AVAILABLE) {
                table.setStatus(DiningTable.TableStatus.RESERVED);
                table.setStatusChangedAt(java.time.LocalDateTime.now());
                tableRepository.save(table);
                System.out.println("Table status updated to RESERVED");
            }
        }

        // Build order
        OrderEntity.Status orderStatus = OrderEntity.Status.NEW;
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
            try {
                orderStatus = OrderEntity.Status.valueOf(dto.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid status value: " + dto.getStatus() + ", using NEW instead");
                orderStatus = OrderEntity.Status.NEW;
            }
        }
        
        // If frontend provided a reservationId attach it
        Reservation reservation = null;
        if (dto.getReservationId() != null) {
            System.out.println("Loading reservation with ID: " + dto.getReservationId());
            reservation = reservationRepository.findById(dto.getReservationId()).orElse(null);
            System.out.println("Reservation loaded: " + reservation);
            if (reservation != null) {
                System.out.println("Reservation ID: " + reservation.getId() + ", Customer: " + reservation.getCustomerName());
            }
        }

        // Attach currently authenticated customer, if any
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : null;
        lehoangquan.example.backend.model.Customer currentCustomer = null;
        if (username != null) {
            currentCustomer = customerRepository.findByUsername(username).orElse(null);
        }

        OrderEntity order = OrderEntity.builder()
            .table(table)
            .reservation(reservation)
            .createdBy(currentCustomer)
            .notes(dto.getNotes())
            .status(orderStatus)
            .items(
                    dto.getItems().stream().map(itemDTO -> {
                        System.out.println("Processing item DTO: menuItemId=" + itemDTO.getMenuItemId() + ", qty=" + itemDTO.getQuantity() + ", price=" + itemDTO.getPrice());
                        MenuItem menuItem = menuItemRepository.findById(itemDTO.getMenuItemId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                                    "MenuItem with id " + itemDTO.getMenuItemId() + " not found"));
                        System.out.println("Menu item loaded: " + menuItem.getName());
                        
                        OrderItem item = OrderItem.builder()
                                .menuItem(menuItem)
                                .quantity(itemDTO.getQuantity() != null ? itemDTO.getQuantity() : 1)
                                .price(itemDTO.getPrice() != null ? itemDTO.getPrice() : menuItem.getPrice())
                                .status(OrderItem.ItemStatus.PENDING)
                                .build();
                        System.out.println("Order item created: " + item);
                        return item;
                    }).collect(Collectors.toList())
                )
                .build();

        System.out.println("Order before create() - reservation: " + order.getReservation());
        OrderEntity saved = create(order);
        System.out.println("Order after create() - reservation: " + saved.getReservation());
        if (saved.getReservation() != null) {
            System.out.println("Saved order has reservation ID: " + saved.getReservation().getId());
        }
        return saved;
    }
}
