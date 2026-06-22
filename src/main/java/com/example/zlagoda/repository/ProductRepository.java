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
                SELECT id_product, category_number, c.category_name, product_name, producer, characteristics
                FROM Product
                INNER JOIN Category AS c ON Product.category_number = c.category_number
                ORDER BY product_name
                """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Product.class));
    }

    public Product findById(Integer id) {
        String sql = "SELECT * FROM Product WHERE id_product = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Product.class), id);
    }

    public void save(Product p) {
        jdbcTemplate.update("INSERT INTO Product (id_product, category_number, product_name, characteristics, producer) VALUES (?, ?, ?, ?, ?)",
            p.getIdProduct(), p.getCategoryNumber(), p.getProductName(), p.getCharacteristics(), p.getProducer());
    }

    public void update(Product p) {
        String sql = """
            UPDATE Product SET
            category_number = ?, product_name = ?, characteristics = ?, producer = ?
            WHERE id_product = ?
            """;

        jdbcTemplate.update(sql, 
            p.getCategoryNumber(), p.getProductName(), p.getCharacteristics(), p.getProducer(),
            p.getIdProduct()
        );
    }

    public boolean exists(Integer idProduct) {
        String query = """
                SELECT COUNT(*)
                FROM Product
                WHERE id_product = ?
                """;
        int res = jdbcTemplate.queryForObject(query, Integer.class, idProduct);
        return (res>0);
    }

    public void delete(Integer id) {
        jdbcTemplate.update("DELETE FROM Product WHERE id_product = ?", id);
    }

}
