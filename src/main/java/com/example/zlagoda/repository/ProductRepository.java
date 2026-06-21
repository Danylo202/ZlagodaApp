package com.example.zlagoda.repository;

import com.example.zlagoda.model.Product;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Product> findAll() {
        String sql = """
                SELECT id_product, category_number, product_name, producer, characteristics
                FROM Product
                ORDER BY product_name
                """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class));
    }
}
