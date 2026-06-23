package com.example.zlagoda;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:hsqldb:mem:zlagoda-customer-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=never"
})
@AutoConfigureMockMvc
class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpDatabase() {
        jdbcTemplate.execute("DROP TABLE Customer_Card IF EXISTS");
        jdbcTemplate.execute("""
                CREATE TABLE Customer_Card (
                    card_number VARCHAR(13) PRIMARY KEY,
                    cust_surname VARCHAR(50) NOT NULL,
                    cust_name VARCHAR(50) NOT NULL,
                    cust_patronymic VARCHAR(50),
                    phone_number VARCHAR(13) NOT NULL,
                    city VARCHAR(50),
                    street VARCHAR(100),
                    zip_code VARCHAR(10),
                    percent INTEGER NOT NULL
                )
                """);

        insertCustomer("C001", "Shevchenko", "Olena", "Petrivna", "+380501111111", "Kyiv", "Khreshchatyk 1", "01001", 5);
        insertCustomer("C002", "Bondarenko", "Petro", null, "+380502222222", null, null, null, 10);
        insertCustomer("C003", "Ivanenko", "Iryna", "Ivanivna", "+380503333333", "Lviv", null, "79000", 5);
    }

    @Test
    @WithMockUser(username = "E01", roles = "MANAGER")
    void managerCanListCustomersSortedBySurnameAndEmptyAddressIsClean() throws Exception {
        MvcResult result = mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/list"))
                .andExpect(model().attributeExists("customers"))
                .andExpect(content().string(containsString("—")))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        assertThat(html.indexOf("Bondarenko")).isLessThan(html.indexOf("Ivanenko"));
        assertThat(html.indexOf("Ivanenko")).isLessThan(html.indexOf("Shevchenko"));
        assertThat(html).doesNotContain(", ,");
    }

    @Test
    @WithMockUser(username = "E02", roles = "CASHIER")
    void cashierCanSearchCustomersBySurname() throws Exception {
        mockMvc.perform(get("/customers").param("surname", "Shev"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Shevchenko")))
                .andExpect(content().string(containsString("+380501111111")))
                .andExpect(content().string(not(containsString("Bondarenko"))));
    }

    @Test
    @WithMockUser(username = "E01", roles = "MANAGER")
    void managerCanFilterCustomersByDiscountPercent() throws Exception {
        mockMvc.perform(get("/customers").param("percent", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Shevchenko")))
                .andExpect(content().string(containsString("Ivanenko")))
                .andExpect(content().string(not(containsString("Bondarenko"))));
    }

    @Test
    @WithMockUser(username = "E02", roles = "CASHIER")
    void cashierCanCreateCustomer() throws Exception {
        mockMvc.perform(post("/customers/save")
                        .with(csrf())
                        .param("mode", "create")
                        .param("cardNumber", "C004")
                        .param("custSurname", "Koval")
                        .param("custName", "Marta")
                        .param("custPatronymic", "Romanivna")
                        .param("phoneNumber", "+380504444444")
                        .param("city", "Odesa")
                        .param("street", "Derybasivska 4")
                        .param("zipCode", "65000")
                        .param("percent", "7"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Customer_Card WHERE card_number = ?", Integer.class, "C004");
        assertThat(count).isOne();
    }

    @Test
    @WithMockUser(username = "E02", roles = "CASHIER")
    void cashierCanEditCustomer() throws Exception {
        mockMvc.perform(post("/customers/save")
                        .with(csrf())
                        .param("mode", "edit")
                        .param("cardNumber", "C001")
                        .param("custSurname", "Shevchenko")
                        .param("custName", "Olena")
                        .param("custPatronymic", "Petrivna")
                        .param("phoneNumber", "+380501111111")
                        .param("city", "Kyiv")
                        .param("street", "Updated 10")
                        .param("zipCode", "01001")
                        .param("percent", "12"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));

        Integer percent = jdbcTemplate.queryForObject("SELECT percent FROM Customer_Card WHERE card_number = ?", Integer.class, "C001");
        String street = jdbcTemplate.queryForObject("SELECT street FROM Customer_Card WHERE card_number = ?", String.class, "C001");
        assertThat(percent).isEqualTo(12);
        assertThat(street).isEqualTo("Updated 10");
    }

    @Test
    @WithMockUser(username = "E01", roles = "MANAGER")
    void invalidPhoneAndNegativePercentAreRejected() throws Exception {
        mockMvc.perform(post("/customers/save")
                        .with(csrf())
                        .param("mode", "create")
                        .param("cardNumber", "C005")
                        .param("custSurname", "Bad")
                        .param("custName", "Client")
                        .param("phoneNumber", "+380501111111999")
                        .param("percent", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/form"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(content().string(containsString("Телефон не може перевищувати 13 символів")))
                .andExpect(content().string(containsString("Відсоток знижки не може бути від&#39;ємним")));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Customer_Card WHERE card_number = ?", Integer.class, "C005");
        assertThat(count).isZero();
    }

    @Test
    @WithMockUser(username = "E02", roles = "CASHIER")
    void cashierCannotDeleteCustomer() throws Exception {
        mockMvc.perform(get("/customers/C001/delete"))
                .andExpect(status().isForbidden());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Customer_Card WHERE card_number = ?", Integer.class, "C001");
        assertThat(count).isOne();
    }

    @Test
    @WithMockUser(username = "E01", roles = "MANAGER")
    void managerCanDeleteCustomer() throws Exception {
        mockMvc.perform(get("/customers/C001/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Customer_Card WHERE card_number = ?", Integer.class, "C001");
        assertThat(count).isZero();
    }

    private void insertCustomer(String cardNumber,
                                String surname,
                                String name,
                                String patronymic,
                                String phone,
                                String city,
                                String street,
                                String zipCode,
                                int percent) {
        jdbcTemplate.update("""
                        INSERT INTO Customer_Card (card_number, cust_surname, cust_name, cust_patronymic,
                                                   phone_number, city, street, zip_code, percent)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                cardNumber, surname, name, patronymic, phone, city, street, zipCode, percent);
    }
}
