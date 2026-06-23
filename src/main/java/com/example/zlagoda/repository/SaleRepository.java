package com.example.zlagoda.repository;

import com.example.zlagoda.model.Sale;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class SaleRepository {
    private final JdbcTemplate jdbcTemplate;

    public SaleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Sale sale) {
        String sql = "INSERT INTO Sale (UPC, check_number, product_number, selling_price) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, 
            sale.getUPC(), 
            sale.getCheckNumber(), 
            sale.getProductNumber(), 
            sale.getSellingPrice()
        );
    }

    public List<Sale> findByCheckNumber(Integer checkNumber) {
        String sql = """
            SELECT S.*, P.product_name 
            FROM (Sale AS S
            INNER JOIN Store_Product AS SP ON S.UPC = SP.UPC)
            INNER JOIN Product AS P ON SP.id_product = P.id_product
            WHERE S.check_number = ?
            """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Sale.class), checkNumber);
    }

    public List<Sale> findAll() {
        String sql = "SELECT * FROM Sale";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Sale.class));
    }
}
