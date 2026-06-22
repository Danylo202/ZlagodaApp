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
                SELECT SP.UPC, SP.UPC_prom AS UPCProm, SP.id_product, SP.selling_price,
                       SP.products_number, SP.promotional_product AS promotional, P.product_name
                FROM Store_Product AS SP
                INNER JOIN Product AS P ON SP.id_product = P.id_product
                ORDER BY SP.products_number DESC
                """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(StoreProduct.class));
    }

    public StoreProduct findByUPC(String UPC) {
        String sql = """
                SELECT UPC, UPC_prom AS UPCProm, id_product, selling_price,
                       products_number, promotional_product AS promotional
                FROM Store_Product
                WHERE UPC = ?
                """;
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(StoreProduct.class), UPC);
    }

    public void save(StoreProduct sp) {
        normalize(sp);
        validate(sp);

        String sql = "INSERT INTO Store_Product (UPC, UPC_prom, id_product, selling_price, products_number, promotional_product) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, sp.getUPC(), sp.getUPCProm(), sp.getIdProduct(), sp.getSellingPrice(),
                    sp.getProductsNumber(), sp.isPromotional());
    }

    public void update(StoreProduct sp) {
        normalize(sp);
        validate(sp);

        String sql = """
            UPDATE Store_Product SET
            UPC_prom = ?, id_product = ?, selling_price = ?, products_number = ?, promotional_product = ?
            WHERE UPC = ?
            """;

        jdbcTemplate.update(sql,
                sp.getUPCProm(), sp.getIdProduct(), sp.getSellingPrice(), sp.getProductsNumber(), sp.isPromotional(),
                sp.getUPC());
    }

    private void normalize(StoreProduct sp) {
        if (sp.getUPCProm() != null && sp.getUPCProm().trim().isEmpty()) {
            sp.setUPCProm(null);
        }
    }

    private void validate(StoreProduct sp) {
        if (!sp.isValid()) {
            throw new IllegalArgumentException("Помилка логіки: акційний статус не збігається з наявністю UPC акційного товару!");
        }
        if (sp.getSellingPrice() == null || sp.getSellingPrice() < 0) {
            throw new IllegalArgumentException("Ціна продажу не може бути від’ємною.");
        }
        if (sp.getProductsNumber() == null || sp.getProductsNumber() < 0) {
            throw new IllegalArgumentException("Кількість товару не може бути від’ємною.");
        }
        if (sp.isPromotional()) {
            StoreProduct parent = findByUPC(sp.getUPCProm());

            if (parent.isPromotional()) {
                throw new RuntimeException("Не можна створювати акцію на акційний товар!");
            }
            if (!parent.getIdProduct().equals(sp.getIdProduct())) {
                throw new RuntimeException("Акція має посилатися на той самий вид товару!");
            }
        }
    }

    public boolean exists(String UPC) {
        String query = "SELECT COUNT(*) FROM Store_Product WHERE UPC = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, UPC);
        return (count != null && count > 0);
    }


    public void delete(String UPC) {
        jdbcTemplate.update("DELETE FROM Store_Product WHERE UPC = ?", UPC);
    }

}
