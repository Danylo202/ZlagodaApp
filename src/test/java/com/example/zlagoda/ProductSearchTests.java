package com.example.zlagoda;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:hsqldb:mem:zlagoda-product-search-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=never"
})
@AutoConfigureMockMvc
class ProductSearchTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpDatabase() {
        jdbcTemplate.execute("DROP TABLE Product IF EXISTS");
        jdbcTemplate.execute("DROP TABLE Category IF EXISTS");

        jdbcTemplate.execute("""
                CREATE TABLE Category (
                    category_number INTEGER PRIMARY KEY,
                    category_name VARCHAR(100)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE Product (
                    id_product INTEGER PRIMARY KEY,
                    category_number INTEGER,
                    product_name VARCHAR(100),
                    characteristics VARCHAR(255),
                    producer VARCHAR(100)
                )
                """);

        jdbcTemplate.update("INSERT INTO Category (category_number, category_name) VALUES (?, ?)", 1, "Молочні продукти");
        jdbcTemplate.update("INSERT INTO Category (category_number, category_name) VALUES (?, ?)", 2, "Хлібобулочні вироби");
        jdbcTemplate.update("INSERT INTO Product (id_product, category_number, product_name, characteristics, producer) VALUES (?, ?, ?, ?, ?)",
                10, 1, "Молоко 2.5%", "Пастеризоване", "Галичина");
        jdbcTemplate.update("INSERT INTO Product (id_product, category_number, product_name, characteristics, producer) VALUES (?, ?, ?, ?, ?)",
                20, 2, "Хліб житній", "Нарізаний", "Київхліб");
    }

    @Test
    @WithMockUser(username = "E02", roles = "CASHIER")
    void productsSearchFiltersByProductName() throws Exception {
        mockMvc.perform(get("/products").param("q", "молоко"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Молоко 2.5%")))
                .andExpect(content().string(not(containsString("Хліб житній"))));
    }

    @Test
    @WithMockUser(username = "E02", roles = "CASHIER")
    void productsSearchAlsoMatchesProducer() throws Exception {
        mockMvc.perform(get("/products").param("q", "Київхліб"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Хліб житній")))
                .andExpect(content().string(not(containsString("Молоко 2.5%"))));
    }
}
