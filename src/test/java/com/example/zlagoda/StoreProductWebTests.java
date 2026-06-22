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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:hsqldb:mem:zlagoda-store-product-web-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.hsqldb.jdbc.JDBCDriver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=never"
})
@AutoConfigureMockMvc
class StoreProductWebTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUpDatabase() {
        jdbcTemplate.execute("DROP TABLE Store_Product IF EXISTS");
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
        jdbcTemplate.execute("""
                CREATE TABLE Store_Product (
                    UPC VARCHAR(20) PRIMARY KEY,
                    UPC_prom VARCHAR(20),
                    id_product INTEGER NOT NULL,
                    products_number INTEGER NOT NULL,
                    promotional_product BOOLEAN NOT NULL,
                    selling_price NUMERIC(10, 2) NOT NULL
                )
                """);

        jdbcTemplate.update("INSERT INTO Category (category_number, category_name) VALUES (?, ?)", 1, "Молочні продукти");
        jdbcTemplate.update("INSERT INTO Product (id_product, category_number, product_name, characteristics, producer) VALUES (?, ?, ?, ?, ?)",
                10, 1, "Молоко 2.5%", "Пастеризоване", "Галичина");
        jdbcTemplate.update("INSERT INTO Store_Product (UPC, UPC_prom, id_product, products_number, promotional_product, selling_price) VALUES (?, ?, ?, ?, ?, ?)",
                "UPC-001", null, 10, 15, false, 42.50);
    }

    @Test
    @WithMockUser(username = "E02", roles = "CASHIER")
    void cashierCanViewStoreProductsButDoesNotSeeManagerActions() throws Exception {
        mockMvc.perform(get("/store-products"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("UPC-001")))
                .andExpect(content().string(containsString("Молоко 2.5%")))
                .andExpect(content().string(containsString("Звичайний")));
    }

    @Test
    @WithMockUser(username = "E01", roles = "MANAGER")
    void managerCanOpenStoreProductForm() throws Exception {
        mockMvc.perform(get("/store-products/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Новий товар у магазині")))
                .andExpect(content().string(containsString("Молоко 2.5%")));
    }

    @Test
    @WithMockUser(username = "E01", roles = "MANAGER")
    void managerCanCreateStoreProductFromWebForm() throws Exception {
        mockMvc.perform(post("/store-products/save")
                        .with(csrf())
                        .param("UPC", "UPC-002")
                        .param("idProduct", "10")
                        .param("sellingPrice", "50.00")
                        .param("productsNumber", "7")
                        .param("promotional", "false")
                        .param("UPCProm", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/store-products"));
    }
}
