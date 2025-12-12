package lehoangquan.example.backend.security;

import lehoangquan.example.backend.repository.EmployeeRepository;
import lehoangquan.example.backend.repository.CustomerRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;

    public CustomUserDetailsService(EmployeeRepository employeeRepository, CustomerRepository customerRepository) {
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try employee first
        return employeeRepository.findByUsername(username).map(e ->
            new User(e.getUsername(), e.getPassword(),
                e.getRoles().stream().map(r -> new SimpleGrantedAuthority(r.getName())).collect(Collectors.toList()))
        ).orElseGet(() ->
            // fallback to customer
            customerRepository.findByUsername(username).map(c ->
                new User(c.getUsername(), c.getPassword(),
                    // customers get ROLE_CUSTOMER authority
                    java.util.List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                )).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username))
        );
    }
}
