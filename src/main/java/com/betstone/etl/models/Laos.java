package com.betstone.etl.models;

import com.betstone.etl.enums.CountryType;
import com.betstone.etl.enums.Operator;

import java.time.LocalDate;

public class Laos extends Pais{

    public static final String CURRENCY = "Thai Baht";
    public static final Operator operator = Operator.BETSTONE;

    public Laos(LocalDate fechaInicial) {
        super(fechaInicial);
        super.setCountryType(CountryType.LAOS);
    }

}
