package com.example.zlagoda.model;

import java.time.LocalDate;
import java.sql.Date;
import lombok.*;

@Data
@NoArgsConstructor
public class Check {
    private Integer checkNumber;
    private String idEmployee; 
    private String cardNumber;
    private LocalDate printDate;
    private Double sumTotal;
    private Double vat;

    public Check(Integer check_number, String id_employee, String card_number, LocalDate print_date, Double sum_total, Double vat) {
        if(vat == sum_total/5) {
            this.checkNumber = check_number;
            this.idEmployee = id_employee;
            this.cardNumber = card_number;
            this.printDate = print_date;
            this.sumTotal = sum_total;
            this.vat = vat;
        }
        else {
            throw new IllegalArgumentException("Логічна помилка: ПДВ має становити 20% ціни!");
        }
    }

    public boolean isValid() {
        if(this.vat == this.sumTotal/5) {
            return true;
        }
        else {
            return false;
        }
    }
}
