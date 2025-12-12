package lehoangquan.example.backend.controller;

import lehoangquan.example.backend.model.Employee;
import lehoangquan.example.backend.model.Customer;
import lehoangquan.example.backend.repository.EmployeeRepository;
import lehoangquan.example.backend.repository.CustomerRepository;
import lehoangquan.example.backend.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider,
                         EmployeeRepository employeeRepository, CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );

            List<String> roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
            String token = tokenProvider.generateToken(auth.getName(), roles);
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new ErrorResponse("Tên đăng nhập hoặc mật khẩu sai"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Tên đăng nhập không được trống"));
        }
        if (req.getPassword() == null || req.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Mật khẩu phải tối thiểu 6 ký tự"));
        }
        if (customerRepository.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Tên đăng nhập đã tồn tại"));
        }

        Customer cust = new Customer();
        cust.setUsername(req.getUsername());
        cust.setPassword(passwordEncoder.encode(req.getPassword()));
        cust.setFullName(req.getFullName());
        cust.setEmail(req.getEmail());
        cust.setPhone(req.getPhone());
        cust.setCreatedAt(java.time.LocalDateTime.now());

        customerRepository.save(cust);
        return ResponseEntity.ok(new MessageResponse("Đăng ký thành công"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(new ErrorResponse("Token không hợp lệ"));
            }
            String token = authHeader.substring(7);
            String username = tokenProvider.getUsername(token);
            
            Employee emp = employeeRepository.findByUsername(username).orElse(null);
            if (emp != null) {
                PublicUserResponse r = new PublicUserResponse(emp.getId(), emp.getUsername(), emp.getFullName(), emp.getEmail(), emp.getPhone(),
                        emp.getRoles().stream().map(lehoangquan.example.backend.model.Role::getName).collect(Collectors.toList()));
                return ResponseEntity.ok(r);
            }

            Customer cust = customerRepository.findByUsername(username).orElse(null);
            if (cust != null) {
                PublicUserResponse r = new PublicUserResponse(cust.getId(), cust.getUsername(), cust.getFullName(), cust.getEmail(), cust.getPhone(),
                        java.util.List.of("ROLE_CUSTOMER"));
                return ResponseEntity.ok(r);
            }

            return ResponseEntity.status(404).body(new ErrorResponse("Không tìm thấy người dùng"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new ErrorResponse("Token không hợp lệ"));
        }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginResponse {
        private final String token;

        public LoginResponse(String token) { this.token = token; }
        public String getToken() { return token; }
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        private String fullName;
        private String email;
        private String phone;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    public static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }

    public static class MessageResponse {
        private final String message;

        public MessageResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }

    public static class PublicUserResponse {
        private Long id;
        private String username;
        private String fullName;
        private String email;
        private String phone;
        private java.util.List<String> roles;

        public PublicUserResponse(Long id, String username, String fullName, String email, String phone, java.util.List<String> roles) {
            this.id = id;
            this.username = username;
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.roles = roles;
        }

        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public java.util.List<String> getRoles() { return roles; }
    }
}
