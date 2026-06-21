package com.example.zlagoda;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:hsqldb:mem:zlagoda-security-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=never"
})
@AutoConfigureMockMvc
class SecurityLoginTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUpDatabase() {
        jdbcTemplate.execute("DROP TABLE Employee IF EXISTS");
        jdbcTemplate.execute("""
                CREATE TABLE Employee (
                    id_employee VARCHAR(10) PRIMARY KEY,
                    password_hash VARCHAR(100) NOT NULL,
                    empl_role VARCHAR(20) NOT NULL
                )
                """);

        jdbcTemplate.update(
                "INSERT INTO Employee (id_employee, password_hash, empl_role) VALUES (?, ?, ?)",
                "E01",
                passwordEncoder.encode("iamastrongpassword"),
                "manager"
        );
        jdbcTemplate.update(
                "INSERT INTO Employee (id_employee, password_hash, empl_role) VALUES (?, ?, ?)",
                "E02",
                passwordEncoder.encode("iamastrongpassword2"),
                "cashier"
        );
    }

    @Test
    void unauthenticatedUserIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void employeeE01CanLoginWithCorrectPassword() throws Exception {
        mockMvc.perform(formLogin().user("E01").password("iamastrongpassword"))
                .andExpect(authenticated().withUsername("E01"));
    }

    @Test
    void employeeE02CanLoginWithCorrectPassword() throws Exception {
        mockMvc.perform(formLogin().user("E02").password("iamastrongpassword2"))
                .andExpect(authenticated().withUsername("E02"));
    }

    @Test
    void loginFailsWithWrongPassword() throws Exception {
        mockMvc.perform(formLogin().user("E01").password("wrongpassword"))
                .andExpect(unauthenticated());
    }

    @Test
    void loginFailsForUnknownEmployee() throws Exception {
        mockMvc.perform(formLogin().user("NO_SUCH_USER").password("whatever"))
                .andExpect(unauthenticated());
    }
}
