package com.previred.ejercicio.model;

import java.math.BigDecimal;

public class Empleado {
    private Long id;                 // ID autogenerado
    private String nombre;           // Nombre del empleado
    private String apellido;         // Apellido del empleado
    private String rut;              // RUT/DNI (único)
    private String cargo;            // Cargo del empleado
    private BigDecimal salarioBase;  // Salario base (>= $400,000)
    private BigDecimal bonos;        // Bonos (<= 50% del salario base)
    private BigDecimal descuentos;   // Descuentos (<= salario base)

    // Constructor vacío (necesario para Gson y JDBC)
    public Empleado() {}

    // Constructor con campos básicos (Parte 1)
    public Empleado(String nombre, String apellido, String rut, String cargo, BigDecimal salarioBase) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.rut = rut;
        this.cargo = cargo;
        this.salarioBase = salarioBase;
        this.bonos = BigDecimal.ZERO;      // Inicializado en 0
        this.descuentos = BigDecimal.ZERO; // Inicializado en 0
    }

    public Empleado(String nombre, String apellido, String rut, String cargo,
                    BigDecimal salarioBase, BigDecimal bonos, BigDecimal descuentos) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.rut = rut;
        this.cargo = cargo;
        this.salarioBase = salarioBase;
        this.bonos = bonos;
        this.descuentos = descuentos;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public BigDecimal getSalarioBase() { return salarioBase; }
    public void setSalarioBase(BigDecimal salarioBase) { this.salarioBase = salarioBase; }

    public BigDecimal getBonos() { return bonos; }
    public void setBonos(BigDecimal bonos) { this.bonos = bonos; }

    public BigDecimal getDescuentos() { return descuentos; }
    public void setDescuentos(BigDecimal descuentos) { this.descuentos = descuentos; }

    // Método para calcular el salario final (Parte 3)
    public BigDecimal calcularSalarioFinal() {
        return salarioBase.add(bonos).subtract(descuentos);
    }
}