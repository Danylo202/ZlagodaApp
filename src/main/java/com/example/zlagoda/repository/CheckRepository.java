package com.example.zlagoda.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import com.example.zlagoda.model.Check;
import com.example.zlagoda.model.StoreProduct;

import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;

@Repository
public class CheckRepository {
    private final JdbcTemplate jdbcTemplate;

    public CheckRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Check> findAll() {
        String sql = """
                SELECT CH.*
                FROM [Check] AS CH
                ORDER BY CH.print_date ASC
                """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Check.class));
    }

    public Check findById(Integer id) {
        String sql = "SELECT * FROM [Check] WHERE check_number = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Check.class), id);
    }

    public Integer save(Check ch) {
        if (!ch.isValid()) {
            throw new IllegalArgumentException("Логічна помилка: ПДВ має становити 20% ціни!");
        }

        String sql = "INSERT INTO [Check] (id_employee, card_number, print_date, sum_total, vat) VALUES (?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"check_number"});
            ps.setString(1, ch.getIdEmployee());
            if (ch.getCardNumber() == null || ch.getCardNumber().isBlank()) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, ch.getCardNumber());
            }
            ps.setDate(3, java.sql.Date.valueOf(ch.getPrintDate()));
            ps.setDouble(4, ch.getSumTotal());
            ps.setDouble(5, ch.getVat());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    public List<Check> findWithFilters(String idEmployee, LocalDate date1, LocalDate date2) {
        StringBuilder sql = new StringBuilder("SELECT CH.* FROM [Check] AS CH WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        // фільтр за працівником (якщо введено)
        if (idEmployee != null && !idEmployee.isEmpty()) {
            sql.append(" AND id_employee = ?");
            params.add(idEmployee);
        }

        // фільтр за датами
        if (date1!=null && date2!=null) {
            sql.append(" AND print_date BETWEEN ? AND ?");
            params.add(java.sql.Date.valueOf(date1));
            params.add(java.sql.Date.valueOf(date2));
        }

        sql.append(" ORDER BY print_date DESC");

        return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(Check.class));
    }

    public Double sumByEmployeeAndPeriod(String idEmployee, LocalDate date1, LocalDate date2) {
        String sql = "SELECT COALESCE(SUM(sum_total), 0) FROM [Check] WHERE id_employee = ? AND print_date BETWEEN ? AND ?";
        return jdbcTemplate.queryForObject(sql, Double.class, idEmployee, Date.valueOf(date1), Date.valueOf(date2));
    }

    public Double sumByPeriod(LocalDate date1, LocalDate date2) {
        String sql = "SELECT COALESCE(SUM(sum_total), 0) FROM [Check] WHERE print_date BETWEEN ? AND ?";
        return jdbcTemplate.queryForObject(sql, Double.class, Date.valueOf(date1), Date.valueOf(date2));
    }

    public void delete(Integer id) {
        jdbcTemplate.update("DELETE FROM [Check] WHERE check_number = ?", id);
    }

}
