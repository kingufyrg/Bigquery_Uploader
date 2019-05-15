package com.betstone.etl.models;

import com.betstone.etl.enums.CountryType;

import java.time.LocalDate;

public class Pais {

    private LocalDate fecha;
    private CountryType countryType;

    public Pais(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public CountryType getCountryType(){ return countryType;}

    protected void setCountryType(CountryType countryType) {
        this.countryType = countryType;
    }

    public void setFecha(LocalDate date) {
        this.fecha = date;
    }
}
