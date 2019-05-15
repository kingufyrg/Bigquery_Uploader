package com.betstone.etl.models;

import com.betstone.etl.enums.CountryType;

import java.time.LocalDate;

public class Mexico extends Pais{

    public static final String CURRENCY = "Mexican Peso";

    public Mexico(LocalDate fechaInicial) {
        super(fechaInicial);
        super.setCountryType(CountryType.MEXICO);
    }

}
