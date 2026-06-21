package com.example.zlagoda.model;

import java.sql.Date;

public class Employee {
    private String idEmployee;
    private String emplSurname;
    private String emplName;
    private String emplPatronymic;
    private String emplRole;
    private Double salary;
    private Date date_of_birth;
    private Date date_of_start;
    private String phoneNumber;
    private String city;
    private String street;
    private String zipCode;
    private String password;

    public Employee(String idEmployee, String emplSurname, String emplName, String emplPatronymic,
                    String emplRole, Double salary, Date date_of_birth, Date date_of_start,
                    String phoneNumber, String city, String street, String zipCode, 
                    String password) {
        this.idEmployee = idEmployee;
        this.emplSurname = emplSurname;
        this.emplName = emplName;
        this.emplPatronymic = emplPatronymic;
        this.emplRole = emplRole;
        this.salary = salary;
        this.date_of_birth = date_of_birth;
        this.date_of_start = date_of_start;
        this.phoneNumber = phoneNumber;
        this.city = city;
        this.street = street;
        this.zipCode = zipCode;
        this.password = password;
    }
    public String getIdEmployee() {
        return this.idEmployee;
    }
    public String getEmplName() {
        return this.emplName;
    }
    public String getEmplSurname() {
        return this.emplSurname;
    }
    public String getEmplPatronymic() {
        return this.emplPatronymic;
    }
    public String getEmplRole() {
        return this.emplRole;
    }
    public Double getSalary() {
        return this.salary;
    }
    public Date getDateBirth() {
        return this.date_of_birth;
    }
    public Date getDateStart() {
        return this.date_of_start;
    }
    public String getPhone() {
        return this.phoneNumber;
    }
    public String getCity() {
        return this.city;
    }
    public String getStreet() {
        return this.street;
    }
    public String getZip() {
        return this.zipCode;
    }
    public String getPassword() {
        return this.password;
    }
}
