package lehoangquan.example.backend.controller;

import lehoangquan.example.backend.model.Invoice;
import lehoangquan.example.backend.model.Payment;
import lehoangquan.example.backend.model.OrderItem;
import lehoangquan.example.backend.repository.InvoiceRepository;
import lehoangquan.example.backend.repository.OrderRepository;
import lehoangquan.example.backend.repository.PaymentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    public BillingController(OrderRepository orderRepository, InvoiceRepository invoiceRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
    }

    @PostMapping("/invoice/{orderId}")
    @Transactional
    public ResponseEntity<?> createInvoice(@PathVariable Long orderId) {
        System.out.println("=== BillingController.createInvoice ===");
        System.out.println("orderId: " + orderId);
        try {
            return orderRepository.findByIdWithItems(orderId).map(order -> {
                System.out.println("Order found: " + order.getId());
                System.out.println("Order total: " + order.getTotal());
                System.out.println("Order items count: " + (order.getItems() != null ? order.getItems().size() : 0));
                
                // Kiểm tra xem Invoice đã tồn tại chưa
                java.util.Optional<Invoice> existingInvoice = invoiceRepository.findByOrderId(orderId);
                if (existingInvoice.isPresent()) {
                    System.out.println("Invoice already exists for order: " + orderId);
                    return ResponseEntity.ok(existingInvoice.get());
                }
                
                // Kiểm tra order có items không
                if (order.getItems() == null || order.getItems().isEmpty()) {
                    System.out.println("ERROR: Order has no items!");
                    return ResponseEntity.badRequest().body(java.util.Map.of("error", "Order must have items"));
                }
                
                // Tính lại total nếu cần
                if (order.getTotal() == null) {
                    System.out.println("Order total is null, calculating from items...");
                    java.math.BigDecimal calculatedTotal = java.math.BigDecimal.ZERO;
                    for (lehoangquan.example.backend.model.OrderItem item : order.getItems()) {
                        if (item.getPrice() != null) {
                            java.math.BigDecimal itemTotal = item.getPrice()
                                    .multiply(java.math.BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 1));
                            calculatedTotal = calculatedTotal.add(itemTotal);
                        }
                    }
                    order.setTotal(calculatedTotal);
                    orderRepository.save(order);
                    System.out.println("Calculated total: " + calculatedTotal);
                }
                
                // Kiểm tra total > 0
                if (order.getTotal() == null || order.getTotal().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    System.out.println("ERROR: Order total must be greater than 0!");
                    return ResponseEntity.badRequest().body(java.util.Map.of("error", "Order total must be greater than 0"));
                }
                
                Invoice inv = Invoice.builder()
                        .order(order)
                        .amount(order.getTotal())
                        .paid(false)
                        .build();
                System.out.println("Invoice before save: " + inv);
                Invoice saved = invoiceRepository.save(inv);
                System.out.println("Invoice saved with ID: " + saved.getId());
                return ResponseEntity.ok(saved);
            }).orElseGet(() -> {
                System.out.println("Order NOT found for ID: " + orderId);
                return ResponseEntity.status(404).body(java.util.Map.of("error", "Order not found with ID: " + orderId));
            });
        } catch (Exception e) {
            System.out.println("Exception in createInvoice: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(java.util.Map.of("error", "Error creating invoice: " + e.getMessage()));
        }
    }

    @PostMapping("/pay/{invoiceId}")
    public ResponseEntity<?> payInvoice(@PathVariable Long invoiceId, @RequestParam BigDecimal amount, @RequestParam String method) {
        return invoiceRepository.findById(invoiceId).map(inv -> {
            Payment p = Payment.builder().invoice(inv).amount(amount).method(method).build();
            paymentRepository.save(p);
            inv.setPaid(true);
            invoiceRepository.save(inv);
            return ResponseEntity.ok(p);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/invoice/{orderId}")
    public ResponseEntity<?> getInvoiceByOrderId(@PathVariable Long orderId) {
        return invoiceRepository.findByOrderId(orderId)
                .map(inv -> ResponseEntity.ok(inv))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
