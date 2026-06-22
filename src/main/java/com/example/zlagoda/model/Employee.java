package com.example.zlagoda.model;

import java.sql.Date;
import lombok.*;

@Data
@NoArgsConstructor 
@AllArgsConstructor
public class Employee {
    private String idEmployee;
    private String emplSurname;
    private String emplName;
    private String emplPatronymic;
    private String emplRole;
    private Double salary;
    private Date dateBirth;
    private Date dateStart;
    private String phoneNumber;
    private String city;
    private String street;
    private String zipCode;
    private String password;
}
