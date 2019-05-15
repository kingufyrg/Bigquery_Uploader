package com.betstone.etl.models;

import com.betstone.etl.enums.CountryType;
import com.betstone.etl.enums.Operator;

import java.time.LocalDate;

public class Thurks extends Pais{

    public static final String CURRENCY = "US Dollar";
    public static final Operator operator = Operator.BETSTONE;

    public Thurks(LocalDate fechaInicial) {
        super(fechaInicial);
        super.setCountryType(CountryType.THURKS);
    }
}
