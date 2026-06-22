package com.example.zlagoda.repository;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.example.zlagoda.model.Employee;

@Repository
public class EmployeeRepository {
    private final JdbcTemplate jdbcTemplate;

    public EmployeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // пошук усіх працівників
    public List<Employee> findAll() {
        String sql = """
                SELECT id_employee, empl_surname, empl_name, empl_patronymic, empl_role, salary,
                       date_of_birth AS dateBirth, date_of_start AS dateStart,
                       phone_number AS phoneNumber, city, street, zip_code, password_hash AS password
                FROM Employee
                ORDER BY empl_surname
                """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Employee.class));
    }

    // пошук працівника за ID
    public Employee findById(String id) {
        String sql = """
                SELECT id_employee, empl_surname, empl_name, empl_patronymic, empl_role, salary,
                       date_of_birth AS dateBirth, date_of_start AS dateStart,
                       phone_number AS phoneNumber, city, street, zip_code, password_hash AS password
                FROM Employee
                WHERE id_employee = ?
                """;
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Employee.class), id);
    }

    // додавання нового працівника
    public void save(Employee e) {
        String sql = """
                    INSERT INTO Employee (id_employee, empl_surname, empl_name, empl_patronymic, empl_role, salary,
                    date_of_birth, date_of_start, phone_number, city, street, zip_code, password_hash)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql, e.getIdEmployee(), e.getEmplSurname(), e.getEmplName(), e.getEmplPatronymic(),
                           e.getEmplRole(), e.getSalary(), e.getDateBirth(), e.getDateStart(), e.getPhoneNumber(),
                           e.getCity(), e.getStreet(), e.getZipCode(), e.getPassword());
    }

    // зміна інформації про працівника
    public void update(Employee e) {
        String sql = """
                    UPDATE Employee SET
                    empl_surname = ?, empl_name = ?, empl_patronymic = ?, empl_role = ?, salary = ?, date_of_birth = ?, date_of_start = ?, phone_number = ?, city = ?, street = ?, zip_code = ?
                    WHERE id_employee = ?
                """;

        jdbcTemplate.update(sql, 
            e.getEmplSurname(), e.getEmplName(), e.getEmplPatronymic(),
            e.getEmplRole(), e.getSalary(), e.getDateBirth(), e.getDateStart(),
            e.getPhoneNumber(), e.getCity(), e.getStreet(), e.getZipCode(),
            e.getIdEmployee()
        );
    }

    // запит№2: працівники, що продали товари всіх категорій
    public List<Employee> findSuperEmployees() {
        String sql = """
                    SELECT E.id_employee, E.empl_surname, E.empl_name, E.empl_patronymic, E.empl_role, E.salary,
                           E.date_of_birth AS dateBirth, E.date_of_start AS dateStart,
                           E.phone_number AS phoneNumber, E.city, E.street, E.zip_code, E.password_hash AS password
                    FROM Employee AS E
                    WHERE NOT EXISTS (
                    SELECT Category.category_number FROM Category
                    WHERE NOT EXISTS (
                        SELECT * FROM ((Check
                        INNER JOIN Sale ON Check.check_number = Sale.check_number)
                        INNER JOIN Store_Product ON Sale.UPC = Store_Product.UPC)
                        INNER JOIN Product ON Store_Product.id_product = Product.id_product
                        WHERE Product.category_number = Category.category_number
                        AND Check.id_employee = E.id_employee
                        )
                    )
                """;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Employee.class));
    }

    // видалення працівника
    public void deleteById(String id) {
        jdbcTemplate.update("DELETE FROM Employee WHERE id_employee = ?", id);
    }
}
