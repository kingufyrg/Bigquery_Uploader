package com.betstone.etl.models;

import com.betstone.etl.enums.CountryType;
import com.betstone.etl.enums.Operator;
import com.betstone.etl.enums.SiteType;

import java.time.LocalDate;

public class Nepal extends Pais{

    public static final String INDIAN_CURRENCY = "Indian Rupee";
    public static final String US_CURRENCY = "US_Dollar";
    private SiteType siteType;

    public Nepal(LocalDate fechaInicial) {
        super(fechaInicial);
        super.setCountryType(CountryType.NEPAL);
    }

    public Nepal(LocalDate yesterday, SiteType siteType) {
        super(yesterday);
        this.siteType = siteType;
        super.setCountryType(CountryType.NEPAL);
    }

    public SiteType getSiteType() {
        return siteType;
    }
}
