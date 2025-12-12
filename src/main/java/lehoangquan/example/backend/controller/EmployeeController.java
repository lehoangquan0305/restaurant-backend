package lehoangquan.example.backend.controller;

import lehoangquan.example.backend.model.Employee;
 
import lehoangquan.example.backend.repository.EmployeeRepository;
import lehoangquan.example.backend.repository.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeController(EmployeeRepository employeeRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public java.util.List<Employee> list() { return employeeRepository.findAll(); }

    @GetMapping("/roles")
    public java.util.List<lehoangquan.example.backend.model.Role> roles(){
        return roleRepository.findAll();
    }

    @PostMapping
    public Employee create(@RequestBody Employee req) {
        if (req.getPassword() != null) req.setPassword(passwordEncoder.encode(req.getPassword()));
        // map incoming roles (may contain only id) to managed Role entities
        if (req.getRoles() == null) req.setRoles(new HashSet<>());
        else {
            java.util.Set<lehoangquan.example.backend.model.Role> mapped = new HashSet<>();
            for (lehoangquan.example.backend.model.Role r : req.getRoles()){
                if (r.getId() != null){
                    roleRepository.findById(r.getId()).ifPresent(mapped::add);
                } else if (r.getName() != null){
                    roleRepository.findByName(r.getName()).ifPresent(mapped::add);
                }
            }
            req.setRoles(mapped);
        }
        return employeeRepository.save(req);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(@PathVariable Long id, @RequestBody Employee req) {
        return employeeRepository.findById(id).map(e -> {
            e.setFullName(req.getFullName());
            e.setEmail(req.getEmail());
            e.setPhone(req.getPhone());
            if (req.getPassword() != null && !req.getPassword().isEmpty()) {
                e.setPassword(passwordEncoder.encode(req.getPassword()));
            }
            // map roles similar to create
            if (req.getRoles() == null) e.setRoles(new HashSet<>());
            else {
                java.util.Set<lehoangquan.example.backend.model.Role> mapped = new HashSet<>();
                for (lehoangquan.example.backend.model.Role r : req.getRoles()){
                    if (r.getId() != null){
                        roleRepository.findById(r.getId()).ifPresent(mapped::add);
                    } else if (r.getName() != null){
                        roleRepository.findByName(r.getName()).ifPresent(mapped::add);
                    }
                }
                e.setRoles(mapped);
            }
            employeeRepository.save(e);
            return ResponseEntity.ok(e);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
