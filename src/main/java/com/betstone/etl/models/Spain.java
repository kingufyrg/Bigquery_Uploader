package com.betstone.etl.models;

import com.betstone.etl.enums.CountryType;
import com.betstone.etl.enums.Operator;

import java.time.LocalDate;

public class Spain extends Pais{

    public static final String CURRENCY = "Euro";
    public static final Operator operator = Operator.BETSTONE;

    public Spain(LocalDate fechaInicial) {
        super(fechaInicial);
        super.setCountryType(CountryType.SPAIN);
    }
}
