package lehoangquan.example.backend.controller;

import lehoangquan.example.backend.model.MenuItem;
import lehoangquan.example.backend.repository.MenuItemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuItemRepository menuRepo;

    public MenuController(MenuItemRepository menuRepo) {
        this.menuRepo = menuRepo;
    }

    @GetMapping
    public List<MenuItem> list() { return menuRepo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItem> get(@PathVariable Long id) {
        return menuRepo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = "multipart/form-data")
    public MenuItem create(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "true") boolean available,
            @RequestPart(required = false) MultipartFile image) throws IOException {
        
        MenuItem item = MenuItem.builder()
                .name(name)
                .description(description)
                .price(price)
                .category(category)
                .available(available)
                .build();
        
        if (image != null && !image.isEmpty()) {
            // Chuyển file thành Base64 string
            String base64Image = "data:" + image.getContentType() + ";base64," + Base64.getEncoder().encodeToString(image.getBytes());
            item.setImage(base64Image);
        }
        
        return menuRepo.save(item);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItem> update(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "true") boolean available,
            @RequestPart(required = false) MultipartFile image) throws IOException {
        
        return menuRepo.findById(id).map(m -> {
            m.setName(name);
            m.setDescription(description);
            m.setPrice(price);
            m.setAvailable(available);
            m.setCategory(category);
            
            // Nếu có upload ảnh mới, cập nhật ảnh
            try {
                if (image != null && !image.isEmpty()) {
                    // Chuyển file thành Base64 string
                    String base64Image = "data:" + image.getContentType() + ";base64," + Base64.getEncoder().encodeToString(image.getBytes());
                    m.setImage(base64Image);
                }
                // Nếu không upload ảnh mới, giữ lại ảnh cũ (không cần làm gì)
            } catch (IOException e) {
                throw new RuntimeException("Error processing image", e);
            }
            
            menuRepo.save(m);
            return ResponseEntity.ok(m);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return menuRepo.findById(id).map(m -> {
            menuRepo.delete(m);
            return ResponseEntity.ok().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}



