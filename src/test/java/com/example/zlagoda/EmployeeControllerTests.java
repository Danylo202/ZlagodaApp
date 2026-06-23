package com.example.zlagoda;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:hsqldb:mem:zlagoda-employee-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=never"
})
@AutoConfigureMockMvc
class EmployeeControllerTests {

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
                    empl_surname VARCHAR(50) NOT NULL,
                    empl_name VARCHAR(50) NOT NULL,
                    empl_patronymic VARCHAR(50),
                    empl_role VARCHAR(20) NOT NULL,
                    salary DECIMAL(10,2) NOT NULL,
                    date_of_birth DATE NOT NULL,
                    date_of_start DATE NOT NULL,
                    phone_number VARCHAR(13),
                    city VARCHAR(50),
                    street VARCHAR(100),
                    zip_code VARCHAR(10),
                    password_hash VARCHAR(100) NOT NULL
                )
                """);

        insertEmployee("E01", "Ivanenko", "Ivan", "Ivanovych", "manager", "25000.00", "1985-01-10", "2020-02-01", "+380501111111", "Kyiv", "Khreshchatyk 1", "01001", "manager-pass");
        insertEmployee("E02", "Shevchenko", "Olena", "Petrivna", "cashier", "15000.00", "1994-05-20", "2022-03-01", "+380502222222", "Lviv", "Rynok 2", "79000", "cashier-pass");
        insertEmployee("E03", "Bondarenko", "Petro", null, "cashier", "16000.00", "1990-07-15", "2021-06-01", "+380503333333", "Odesa", "Derybasivska 3", "65000", "cashier-pass-2");
    }

    @Test
    @WithMockUser(username = "E01", roles = "MANAGER")
    void managerCanListEmployeesSortedBySurname() throws Exception {
        MvcResult result = mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/list"))
                .andExpect(model().attributeExists("employees"))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html.indexOf("Bondarenko")).isLessThan(html.indexOf("Ivanenko"));
        assertThat(html.indexOf("Ivanenko")).isLessThan(html.indexOf("Shevchenko"));
    }

    @Test
    @WithMockUser(username = "E01", roles = "MANAGER")
    void managerCanSearchEmployeeContactInfoBySurname() throws Exception {
        mockMvc.perform(get("/employees").param("surname", "Shev"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Shevchenko")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("+380502222222")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Lviv")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Ivanenko"))));
    }

    @Test
    @WithMockUser(username = "E01", roles = "MANAGER")
    void managerCanFilterOnlyCashiers() throws Exception {
        mockMvc.perform(get("/employees").param("role", "cashier"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Shevchenko")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Bondarenko")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Ivanenko"))));
    }

    @Test
    @WithMockUser(username = "E01", roles = "MANAGER")
    void managerCanCreateEmployeeAndPasswordIsHashed() throws Exception {
        mockMvc.perform(post("/employees/save")
                        .with(csrf())
                        .param("mode", "create")
                        .param("idEmployee", "E04")
                        .param("emplSurname", "Koval")
                        .param("emplName", "Marta")
                        .param("emplPatronymic", "Romanivna")
                        .param("emplRole", "Касир")
                        .param("salary", "17000.00")
                        .param("dateBirth", "1998-09-01")
                        .param("dateStart", "2024-01-15")
                        .param("phoneNumber", "+380504444444")
                        .param("city", "Kyiv")
                        .param("street", "Test 4")
                        .param("zipCode", "04000")
                        .param("password", "new-employee-pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        String hash = jdbcTemplate.queryForObject("SELECT password_hash FROM Employee WHERE id_employee = ?", String.class, "E04");
        assertThat(hash).isNotEqualTo("new-employee-pass");
        assertThat(passwordEncoder.matches("new-employee-pass", hash)).isTrue();
    }

    @Test
    @WithMockUser(username = "E01", roles = "MANAGER")
    void unsupportedEmployeeRoleIsRejected() throws Exception {
        mockMvc.perform(post("/employees/save")
                        .with(csrf())
                        .param("mode", "create")
                        .param("idEmployee", "E06")
                        .param("emplSurname", "Role")
                        .param("emplName", "Hacker")
                        .param("emplRole", "admin")
                        .param("salary", "10000.00")
                        .param("dateBirth", "1990-01-01")
                        .param("dateStart", "2024-01-15")
                        .param("phoneNumber", "+380506666666")
                        .param("password", "pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Посада має бути лише Менеджер або Касир")));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Employee WHERE id_employee = ?", Integer.class, "E06");
        assertThat(count).isZero();
    }

    @Test
    @WithMockUser(username = "E01", roles = "MANAGER")
    void underageEmployeeIsRejected() throws Exception {
        mockMvc.perform(post("/employees/save")
                        .with(csrf())
                        .param("mode", "create")
                        .param("idEmployee", "E05")
                        .param("emplSurname", "Young")
                        .param("emplName", "User")
                        .param("emplRole", "Касир")
                        .param("salary", "10000.00")
                        .param("dateBirth", "2015-01-01")
                        .param("dateStart", "2024-01-15")
                        .param("phoneNumber", "+380505555555")
                        .param("password", "pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("не молодшим 18 років")));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Employee WHERE id_employee = ?", Integer.class, "E05");
        assertThat(count).isZero();
    }

    @Test
    @WithMockUser(username = "E01", roles = "MANAGER")
    void editEmployeeWithoutPasswordKeepsExistingPasswordHash() throws Exception {
        String oldHash = jdbcTemplate.queryForObject("SELECT password_hash FROM Employee WHERE id_employee = ?", String.class, "E02");

        mockMvc.perform(post("/employees/save")
                        .with(csrf())
                        .param("mode", "edit")
                        .param("idEmployee", "E02")
                        .param("emplSurname", "Shevchenko")
                        .param("emplName", "Olena")
                        .param("emplPatronymic", "Petrivna")
                        .param("emplRole", "Касир")
                        .param("salary", "18000.00")
                        .param("dateBirth", "1994-05-20")
                        .param("dateStart", "2022-03-01")
                        .param("phoneNumber", "+380502222222")
                        .param("city", "Lviv")
                        .param("street", "Updated Street 5")
                        .param("zipCode", "79000")
                        .param("password", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        String newHash = jdbcTemplate.queryForObject("SELECT password_hash FROM Employee WHERE id_employee = ?", String.class, "E02");
        Double salary = jdbcTemplate.queryForObject("SELECT salary FROM Employee WHERE id_employee = ?", Double.class, "E02");
        String street = jdbcTemplate.queryForObject("SELECT street FROM Employee WHERE id_employee = ?", String.class, "E02");

        assertThat(newHash).isEqualTo(oldHash);
        assertThat(salary).isEqualTo(18000.00);
        assertThat(street).isEqualTo("Updated Street 5");
    }

    @Test
    @WithMockUser(username = "E01", roles = "MANAGER")
    void managerCannotDeleteOwnAccount() throws Exception {
        mockMvc.perform(get("/employees/E01/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Employee WHERE id_employee = ?", Integer.class, "E01");
        assertThat(count).isOne();
    }

    @Test
    @WithMockUser(username = "E02", roles = "CASHIER")
    void cashierCanViewOwnCardButCannotListAllEmployees() throws Exception {
        mockMvc.perform(get("/employees/me"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Shevchenko")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Ivanenko"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("href=\"/employees\""))));

        mockMvc.perform(get("/employees"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/me"));
    }

    private void insertEmployee(String id,
                                String surname,
                                String name,
                                String patronymic,
                                String role,
                                String salary,
                                String birthDate,
                                String startDate,
                                String phone,
                                String city,
                                String street,
                                String zipCode,
                                String rawPassword) {
        jdbcTemplate.update("""
                        INSERT INTO Employee (id_employee, empl_surname, empl_name, empl_patronymic, empl_role, salary,
                                              date_of_birth, date_of_start, phone_number, city, street, zip_code, password_hash)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                id, surname, name, patronymic, role, salary, birthDate, startDate, phone, city, street, zipCode,
                passwordEncoder.encode(rawPassword));
    }
}
