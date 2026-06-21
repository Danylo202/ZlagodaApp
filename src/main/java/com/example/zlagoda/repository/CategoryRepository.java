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
        String sql = "SELECT category_number, category_name FROM Category";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Category.class));
    }
}
