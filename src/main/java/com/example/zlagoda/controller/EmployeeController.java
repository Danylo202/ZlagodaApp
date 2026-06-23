package com.example.zlagoda.controller;

import com.example.zlagoda.model.Employee;
import com.example.zlagoda.repository.EmployeeRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class EmployeeController {
    private static final String MODE_CREATE = "create";
    private static final String MODE_EDIT = "edit";
    private static final String MODE_VIEW = "view";
    private static final List<String> ROLE_OPTIONS = List.of("Менеджер", "Касир");

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeController(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/employees")
    public String list(@RequestParam(required = false) String role,
                       @RequestParam(required = false) String surname,
                       Model model,
                       Authentication authentication) {
        if (hasRole(authentication, "ROLE_CASHIER") && !hasRole(authentication, "ROLE_MANAGER")) {
            return "redirect:/employees/me";
        }

        List<Employee> employees;
        if (surname != null && !surname.trim().isEmpty()) {
            employees = employeeRepository.findBySurname(surname.trim());
        } else if ("cashier".equalsIgnoreCase(role)) {
            employees = employeeRepository.findCashiers();
        } else if ("manager".equalsIgnoreCase(role)) {
            employees = employeeRepository.findManagers();
        } else {
            employees = employeeRepository.findAll();
        }

        model.addAttribute("employees", employees);
        model.addAttribute("role", role == null ? "all" : role);
        model.addAttribute("surname", surname);
        model.addAttribute("employeeId", authentication.getName());
        return "employees/list";
    }

    @GetMapping("/employees/me")
    public String me(Model model, Authentication authentication) {
        Employee employee = employeeRepository.findById(authentication.getName());
        employee.setEmplRole(normalizeRoleForForm(employee.getEmplRole()));
        addFormAttributes(model, employee, MODE_VIEW);
        return "employees/form";
    }

    @GetMapping("/employees/new")
    public String newForm(Model model) {
        addFormAttributes(model, new Employee(), MODE_CREATE);
        return "employees/form";
    }

    @GetMapping("/employees/{id}/edit")
    public String edit(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Employee employee = employeeRepository.findById(id);
            employee.setEmplRole(normalizeRoleForForm(employee.getEmplRole()));
            employee.setPassword(null);
            addFormAttributes(model, employee, MODE_EDIT);
            return "employees/form";
        } catch (EmptyResultDataAccessException ex) {
            redirectAttributes.addFlashAttribute("error", "Працівника не знайдено.");
            return "redirect:/employees";
        }
    }

    @GetMapping("/employees/{id}/delete")
    public String delete(@PathVariable String id, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (authentication.getName().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "Неможливо видалити власний обліковий запис.");
            return "redirect:/employees";
        }

        try {
            employeeRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Працівника видалено.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Неможливо видалити працівника: існують пов'язані чеки або інші дані.");
        }
        return "redirect:/employees";
    }

    @PostMapping("/employees/save")
    public String save(@ModelAttribute Employee employee,
                       @RequestParam(defaultValue = MODE_CREATE) String mode,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        boolean create = MODE_CREATE.equals(mode);
        String normalizedRole = normalizeRoleForSave(employee.getEmplRole());
        List<String> errors = validate(employee, create, normalizedRole);

        if (create && employeeRepository.existsById(employee.getIdEmployee())) {
            errors.add("Працівник з таким ID вже існує.");
        }

        if (!errors.isEmpty()) {
            employee.setPassword(null);
            addFormAttributes(model, employee, mode);
            model.addAttribute("errors", errors);
            return "employees/form";
        }

        employee.setEmplRole(normalizedRole);

        try {
            if (create) {
                employee.setPassword(passwordEncoder.encode(employee.getPassword()));
                employeeRepository.save(employee);
                redirectAttributes.addFlashAttribute("success", "Працівника додано.");
            } else {
                if (employee.getPassword() != null && !employee.getPassword().isBlank()) {
                    employee.setPassword(passwordEncoder.encode(employee.getPassword()));
                    employeeRepository.updateWithPassword(employee);
                } else {
                    employeeRepository.update(employee);
                }
                redirectAttributes.addFlashAttribute("success", "Дані працівника оновлено.");
            }
        } catch (Exception ex) {
            employee.setPassword(null);
            addFormAttributes(model, employee, mode);
            model.addAttribute("errors", List.of("Не вдалося зберегти працівника: " + ex.getMessage()));
            return "employees/form";
        }

        return "redirect:/employees";
    }

    private List<String> validate(Employee employee, boolean create, String normalizedRole) {
        List<String> errors = new ArrayList<>();
        if (isBlank(employee.getIdEmployee())) errors.add("Вкажіть ID працівника.");
        if (isBlank(employee.getEmplSurname())) errors.add("Вкажіть прізвище.");
        if (isBlank(employee.getEmplName())) errors.add("Вкажіть ім'я.");
        if (isBlank(employee.getEmplRole())) {
            errors.add("Вкажіть посаду.");
        } else if (normalizedRole == null) {
            errors.add("Посада має бути лише Менеджер або Касир.");
        }
        if (employee.getSalary() == null || employee.getSalary() < 0) errors.add("Зарплата не може бути від'ємною.");
        if (employee.getDateBirth() == null) errors.add("Вкажіть дату народження.");
        if (employee.getDateStart() == null) errors.add("Вкажіть дату початку роботи.");
        if (employee.getPhoneNumber() != null && employee.getPhoneNumber().length() > 13) errors.add("Телефон не може перевищувати 13 символів.");
        if (create && isBlank(employee.getPassword())) errors.add("Вкажіть початковий пароль.");

        if (employee.getDateBirth() != null) {
            LocalDate latestAdultBirthDate = LocalDate.now().minusYears(18);
            LocalDate birthDate = ((Date) employee.getDateBirth()).toLocalDate();
            if (birthDate.isAfter(latestAdultBirthDate)) {
                errors.add("Працівник має бути не молодшим 18 років.");
            }
        }
        return errors;
    }

    private void addFormAttributes(Model model, Employee employee, String mode) {
        model.addAttribute("employee", employee);
        model.addAttribute("mode", mode);
        model.addAttribute("roleOptions", ROLE_OPTIONS);
        model.addAttribute("maxBirthDate", maxBirthDate());
        model.addAttribute("formAction", MODE_VIEW.equals(mode) ? "/employees/me" : "/employees/save");
        model.addAttribute("formMethod", MODE_VIEW.equals(mode) ? "get" : "post");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()));
    }

    private String normalizeRoleForForm(String role) {
        String normalizedRole = normalizeRoleForSave(role);
        return normalizedRole == null ? role : normalizedRole;
    }

    private String normalizeRoleForSave(String role) {
        if (role == null) return null;

        String trimmedRole = role.trim();
        if (trimmedRole.equalsIgnoreCase("manager") || trimmedRole.equalsIgnoreCase("менеджер")) return "Менеджер";
        if (trimmedRole.equalsIgnoreCase("cashier") || trimmedRole.equalsIgnoreCase("касир")) return "Касир";

        return null;
    }

    private LocalDate maxBirthDate() {
        return LocalDate.now().minusYears(18);
    }
}
