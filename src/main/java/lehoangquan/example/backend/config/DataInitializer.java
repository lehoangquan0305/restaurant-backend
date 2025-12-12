package lehoangquan.example.backend.config;

import lehoangquan.example.backend.model.*;
import lehoangquan.example.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(
            RoleRepository roleRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder,
            DiningTableRepository tableRepository,
            MenuItemRepository menuItemRepository,
            OrderRepository orderRepository) {
        return args -> {
            // Create roles
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
            Role waiterRole = roleRepository.findByName("ROLE_WAITER").orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_WAITER").build()));
            roleRepository.findByName("ROLE_CHEF").orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_CHEF").build()));

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

            // Create waiter user
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

            // Create test dining tables
            List<DiningTable> tables = tableRepository.findAll();
            if (tables.isEmpty()) {
                for (int i = 1; i <= 6; i++) {
                    tableRepository.save(DiningTable.builder()
                            .name("Bàn " + i)
                            .capacity(4)
                            .status(DiningTable.TableStatus.AVAILABLE)
                            .build());
                }
            }

            // Create menu items
            List<MenuItem> menuItems = menuItemRepository.findAll();
            if (menuItems.isEmpty()) {
                menuItemRepository.save(MenuItem.builder().name("Phở Bò").price(BigDecimal.valueOf(50000)).build());
                menuItemRepository.save(MenuItem.builder().name("Cơm Tấm").price(BigDecimal.valueOf(40000)).build());
                menuItemRepository.save(MenuItem.builder().name("Bánh Mì").price(BigDecimal.valueOf(20000)).build());
                menuItemRepository.save(MenuItem.builder().name("Nem Rán").price(BigDecimal.valueOf(35000)).build());
                menuItemRepository.save(MenuItem.builder().name("Cà Phê").price(BigDecimal.valueOf(15000)).build());
            }

            // Create test order with RESERVED table
            if (orderRepository.findAll().isEmpty()) {
                DiningTable reservedTable = tableRepository.findAll().stream()
                        .findFirst()
                        .orElseThrow();
                reservedTable.setStatus(DiningTable.TableStatus.RESERVED);
                reservedTable.setStatusChangedAt(LocalDateTime.now());
                tableRepository.save(reservedTable);

                List<MenuItem> items = menuItemRepository.findAll();
                
                // Create order with items
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
                item1.setOrder(order);

                OrderItem item2 = OrderItem.builder()
                        .menuItem(items.get(1))
                        .quantity(1)
                        .price(items.get(1).getPrice())
                        .status(OrderItem.ItemStatus.DONE)
                        .build();
                item2.setOrder(order);

                order.getItems().add(item1);
                order.getItems().add(item2);
                order.setTotal(item1.getPrice().multiply(java.math.BigDecimal.valueOf(2)).add(item2.getPrice()));

                orderRepository.save(order);
            }
        };
    }
}
