package lehoangquan.example.backend.config;

import lehoangquan.example.backend.model.*;
import lehoangquan.example.backend.repository.*;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Configuration
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final DiningTableRepository tableRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;

    public DataInitializer(
            RoleRepository roleRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder,
            DiningTableRepository tableRepository,
            MenuItemRepository menuItemRepository,
            OrderRepository orderRepository) {
        this.roleRepository = roleRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.tableRepository = tableRepository;
        this.menuItemRepository = menuItemRepository;
        this.orderRepository = orderRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {

        System.out.println("🚀 Application Ready -> Initializing demo data...");

        // Create roles
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
        Role waiterRole = roleRepository.findByName("ROLE_WAITER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_WAITER").build()));
        roleRepository.findByName("ROLE_CHEF")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_CHEF").build()));

        // Create admin user
        if (employeeRepository.findByUsername("admin").isEmpty()) {
            Employee admin = Employee.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .fullName("Administrator")
                    .roles(new HashSet<>())
                    .build();
            admin.getRoles().add(adminRole);
            employeeRepository.save(admin);
        }

        // Create waiter
        if (employeeRepository.findByUsername("waiter1").isEmpty()) {
            Employee waiter = Employee.builder()
                    .username("waiter1")
                    .password(passwordEncoder.encode("waiter"))
                    .fullName("Waiter 1")
                    .roles(new HashSet<>())
                    .build();
            waiter.getRoles().add(waiterRole);
            employeeRepository.save(waiter);
        }

        // Create dining tables
        if (tableRepository.findAll().isEmpty()) {
            for (int i = 1; i <= 6; i++) {
                tableRepository.save(DiningTable.builder()
                        .name("Bàn " + i)
                        .capacity(4)
                        .status(DiningTable.TableStatus.AVAILABLE)
                        .build());
            }
        }

        // Create menu items
        if (menuItemRepository.findAll().isEmpty()) {
            menuItemRepository.save(MenuItem.builder().name("Phở Bò").price(BigDecimal.valueOf(50000)).build());
            menuItemRepository.save(MenuItem.builder().name("Cơm Tấm").price(BigDecimal.valueOf(40000)).build());
            menuItemRepository.save(MenuItem.builder().name("Bánh Mì").price(BigDecimal.valueOf(20000)).build());
            menuItemRepository.save(MenuItem.builder().name("Nem Rán").price(BigDecimal.valueOf(35000)).build());
            menuItemRepository.save(MenuItem.builder().name("Cà Phê").price(BigDecimal.valueOf(15000)).build());
        }

        // Create test order
        if (orderRepository.findAll().isEmpty()) {

            DiningTable reservedTable = tableRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow();
            reservedTable.setStatus(DiningTable.TableStatus.RESERVED);
            reservedTable.setStatusChangedAt(LocalDateTime.now());
            tableRepository.save(reservedTable);

            List<MenuItem> items = menuItemRepository.findAll();

            OrderEntity order = OrderEntity.builder()
                    .table(reservedTable)
                    .notes("Order khách VIP")
                    .status(OrderEntity.Status.NEW)
                    .createdAt(LocalDateTime.now())
                    .build();

            OrderItem item1 = OrderItem.builder()
                    .menuItem(items.get(0))
                    .quantity(2)
                    .price(items.get(0).getPrice())
                    .status(OrderItem.ItemStatus.DONE)
                    .build();

            OrderItem item2 = OrderItem.builder()
                    .menuItem(items.get(1))
                    .quantity(1)
                    .price(items.get(1).getPrice())
                    .status(OrderItem.ItemStatus.DONE)
                    .build();

            item1.setOrder(order);
            item2.setOrder(order);

            order.getItems().add(item1);
            order.getItems().add(item2);
            order.setTotal(
                    item1.getPrice().multiply(BigDecimal.valueOf(2))
                            .add(item2.getPrice())
            );

            orderRepository.save(order);
        }
    }
}
