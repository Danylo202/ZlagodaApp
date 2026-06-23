package com.example.zlagoda.repository;

import com.example.zlagoda.model.Customer;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CustomerRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final String CUSTOMER_SELECT = """
            SELECT card_number AS cardNumber, cust_surname AS custSurname, cust_name AS custName,
                   cust_patronymic AS custPatronymic, phone_number AS phoneNumber,
                   city, street, zip_code AS zipCode, percent
            FROM Customer_Card
            """;

    public CustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Customer> findAll() {
        return jdbcTemplate.query(CUSTOMER_SELECT + " ORDER BY cust_surname, cust_name", new BeanPropertyRowMapper<>(Customer.class));
    }

    public List<Customer> findBySurname(String surname) {
        return jdbcTemplate.query(
                CUSTOMER_SELECT + " WHERE cust_surname LIKE ? ORDER BY cust_surname, cust_name",
                new BeanPropertyRowMapper<>(Customer.class),
                "%" + surname + "%"
        );
    }

    public List<Customer> findBySurnameAndPercent(String surname, Integer percent) {
        return jdbcTemplate.query(
                CUSTOMER_SELECT + " WHERE cust_surname LIKE ? AND percent = ? ORDER BY cust_surname, cust_name",
                new BeanPropertyRowMapper<>(Customer.class),
                "%" + surname + "%",
                percent
        );
    }

    public List<Customer> findByPercent(Integer percent) {
        return jdbcTemplate.query(
                CUSTOMER_SELECT + " WHERE percent = ? ORDER BY cust_surname, cust_name",
                new BeanPropertyRowMapper<>(Customer.class),
                percent
        );
    }

    public Customer findByCardNumber(String cardNumber) {
        return jdbcTemplate.queryForObject(
                CUSTOMER_SELECT + " WHERE card_number = ?",
                new BeanPropertyRowMapper<>(Customer.class),
                cardNumber
        );
    }

    public boolean existsByCardNumber(String cardNumber) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Customer_Card WHERE card_number = ?", Integer.class, cardNumber);
        return count != null && count > 0;
    }

    public void save(Customer customer) {
        String sql = """
                INSERT INTO Customer_Card (card_number, cust_surname, cust_name, cust_patronymic,
                                           phone_number, city, street, zip_code, percent)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql,
                customer.getCardNumber(), customer.getCustSurname(), customer.getCustName(), customer.getCustPatronymic(),
                customer.getPhoneNumber(), customer.getCity(), customer.getStreet(), customer.getZipCode(), customer.getPercent());
    }

    public void update(Customer customer) {
        String sql = """
                UPDATE Customer_Card SET
                    cust_surname = ?, cust_name = ?, cust_patronymic = ?, phone_number = ?,
                    city = ?, street = ?, zip_code = ?, percent = ?
                WHERE card_number = ?
                """;
        jdbcTemplate.update(sql,
                customer.getCustSurname(), customer.getCustName(), customer.getCustPatronymic(), customer.getPhoneNumber(),
                customer.getCity(), customer.getStreet(), customer.getZipCode(), customer.getPercent(), customer.getCardNumber());
    }

    public void deleteByCardNumber(String cardNumber) {
        jdbcTemplate.update("DELETE FROM Customer_Card WHERE card_number = ?", cardNumber);
    }
}
