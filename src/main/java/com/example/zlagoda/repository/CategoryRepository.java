package com.example.zlagoda.repository;

import com.example.zlagoda.model.Category;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CategoryRepository {
    private final JdbcTemplate jdbcTemplate;

    public CategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Category> findAll() {
        String sql = "SELECT category_number, category_name FROM Category ORDER BY category_name";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Category.class));
    }

    public Category findById(Integer id) {
        String sql = "SELECT category_number, category_name FROM Category WHERE category_number = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Category.class), id);
    }

    public void save(Category category) {
        String sql = "INSERT INTO Category (category_number, category_name) VALUES (?, ?)";
        jdbcTemplate.update(sql, category.getCategoryNumber(), category.getCategoryName());
    }

    public void update(Category category) {
        String sql = "UPDATE Category SET category_name = ? WHERE category_number = ?";
        jdbcTemplate.update(sql, category.getCategoryName(), category.getCategoryNumber());
    }

    public void deleteById(Integer id) {
        jdbcTemplate.update("DELETE FROM Category WHERE category_number = ?", id);
    }
}
