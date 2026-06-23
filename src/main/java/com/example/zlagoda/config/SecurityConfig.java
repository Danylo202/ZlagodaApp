package com.example.zlagoda.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Locale;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(JdbcTemplate jdbcTemplate) {
        return username -> {
            try {
                return jdbcTemplate.queryForObject(
                        "SELECT id_employee, password_hash, empl_role FROM Employee WHERE id_employee = ?",
                        (rs, rowNum) -> {
                            String authority = toAuthority(rs.getString("empl_role"));

                            return new User(
                                    rs.getString("id_employee"),
                                    rs.getString("password_hash"),
                                    List.of(new SimpleGrantedAuthority(authority))
                            );
                        },
                        username
                );
            } catch (EmptyResultDataAccessException ex) {
                throw new UsernameNotFoundException("Employee not found: " + username, ex);
            }
        };
    }

    private static String toAuthority(String employeeRole) {
        String role = employeeRole == null ? "" : employeeRole.trim().toLowerCase(Locale.ROOT);

        if (role.equals("manager") || role.equals("менеджер")) {
            return "ROLE_MANAGER";
        }
        if (role.equals("cashier") || role.equals("касир")) {
            return "ROLE_CASHIER";
        }

        return "ROLE_" + role.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/img/**", "/fonts/**").permitAll()
                        .requestMatchers("/login", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/employees").hasAnyRole("MANAGER", "CASHIER")
                        .requestMatchers("/employees/me").hasAnyRole("MANAGER", "CASHIER")
                        .requestMatchers("/employees/**").hasRole("MANAGER")
                        .requestMatchers("/reports/**").hasRole("MANAGER")

                        .requestMatchers(HttpMethod.GET, "/checks/new").hasRole("CASHIER")
                        .requestMatchers(HttpMethod.GET, "/checks/*/delete").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/checks", "/checks/**").hasAnyRole("MANAGER", "CASHIER")
                        .requestMatchers(HttpMethod.POST, "/checks", "/checks/**").hasRole("CASHIER")

                        .requestMatchers(HttpMethod.GET, "/customers/*/delete").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/customers", "/customers/**").hasAnyRole("MANAGER", "CASHIER")
                        .requestMatchers(HttpMethod.POST, "/customers", "/customers/**").hasAnyRole("MANAGER", "CASHIER")

                        .requestMatchers(HttpMethod.GET, "/categories/new", "/categories/*/edit", "/categories/*/delete").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.POST, "/categories", "/categories/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/categories").hasAnyRole("MANAGER", "CASHIER")

                        .requestMatchers(HttpMethod.GET, "/products/new", "/products/edit/**", "/products/delete/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.POST, "/products/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/products").hasAnyRole("MANAGER", "CASHIER")

                        .requestMatchers(HttpMethod.GET, "/store-products/new", "/store-products/edit/**", "/store-products/delete/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.POST, "/store-products/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/store-products", "/store-products/**").hasAnyRole("MANAGER", "CASHIER")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> response.sendRedirect(request.getContextPath() + "/"))
                        .permitAll())
                .logout(logout -> logout.permitAll())
                .build();
    }
}
