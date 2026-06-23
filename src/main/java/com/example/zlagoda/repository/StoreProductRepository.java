package com.example.zlagoda.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.example.zlagoda.model.Product;
import com.example.zlagoda.model.StoreProduct;

@Repository
public class StoreProductRepository {
    private final JdbcTemplate jdbcTemplate;

    public StoreProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<StoreProduct> findAll() {
        String sql = """
                SELECT SP.*, P.product_name FROM Store_Product AS SP
                INNER JOIN Product AS P ON SP.id_product = P.id_product
                ORDER BY SP.products_number DESC
                """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(StoreProduct.class));
    }

    public StoreProduct findByUPC(String UPC) {
        String sql = """
                SELECT SP.*, P.product_name FROM Store_Product AS SP
                INNER JOIN Product AS P ON SP.id_product = P.id_product
                WHERE UPC = ?
                """;
    
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(StoreProduct.class), UPC);
    }

    public void save(StoreProduct sp) {
        if (!sp.isValid()) {
            throw new IllegalArgumentException("Помилка логіки: акційний статус не збігається з наявністю UPCProm!");
        }
        if (sp.isPromotionalProduct()) {
            StoreProduct parent = findByUPC(sp.getUPCProm());

            // акція на акцію - заборонено
            if (parent.isPromotionalProduct()) {
                throw new IllegalArgumentException("Не можна створювати акцію на акційний товар!");
            }

            // перевірка випадку, коли "батько" посилається на інший ID товару
            if (!parent.getIdProduct().equals(sp.getIdProduct())) {
                throw new IllegalArgumentException("Акція має посилатися на той самий вид товару!");
            }
        }
        
        String sql = "INSERT INTO Store_Product (UPC, UPC_prom, id_product, selling_price, products_number, promotional_product) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, sp.getUPC(), sp.getUPCProm(), sp.getIdProduct(), sp.getSellingPrice(), 
                    sp.getProductsNumber(), sp.isPromotionalProduct());
    }

    public List<StoreProduct> findWithFilters(String upc, Boolean isPromo, String sortBy) {
        StringBuilder sql = new StringBuilder(
            "SELECT SP.*, P.product_name FROM Store_Product AS SP " +
            "INNER JOIN Product AS P ON SP.id_product = P.id_product WHERE 1=1 "
        );

        // фільтр за UPC (якщо введено)
        if (upc != null && !upc.isEmpty()) {
            sql.append(" AND SP.UPC = '").append(upc).append("'");
        }

        // беремо акційні або неакційні товари
        if (isPromo != null) {
            sql.append(" AND SP.promotional_product = ").append(isPromo);
        }

        // сортування
        if ("name".equals(sortBy)) {
            sql.append(" ORDER BY P.product_name ASC");
        }
        else if ("quantity".equals(sortBy)) {
            sql.append(" ORDER BY SP.products_number DESC");
        }
        else {
            sql.append("ORDER BY SP.UPC ASC");
        }

        return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(StoreProduct.class));
    }

    public void update(StoreProduct sp) {
        String sql = """
            UPDATE Store_Product SET
            selling_price = ?, products_number = ?
            WHERE UPC = ?
            """;

        jdbcTemplate.update(sql, sp.getSellingPrice(), sp.getProductsNumber(), sp.getUPC());
    }

    public boolean exists(String UPC) {
        String query = "SELECT COUNT(*) FROM Store_Product WHERE UPC = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, UPC);
        return (count != null && count > 0);
    }

    public void decreaseQuantity(String UPC, Integer number) {
        String sql = "UPDATE Store_Product SET products_number = products_number - ? WHERE UPC = ?";
        try {
            jdbcTemplate.update(sql, number, UPC);
        }
        catch(Exception e) {
            throw new IllegalArgumentException("Нема стільки товару!");
        }
    }

    public void delete(String UPC) {
        jdbcTemplate.update("DELETE FROM Store_Product WHERE UPC = ?", UPC);
    }

}
