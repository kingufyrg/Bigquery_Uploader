package com.betstone.etl.enums;

public enum SiteType {
    TIGER_PALACE("Tiger Palace", CountryType.NEPAL),
    SHANGRI("Shangri- La", CountryType.NEPAL),
    MILLIONAIRES_CLUB("Millionaires Club", CountryType.NEPAL);

    private final String siteName;
    private final CountryType countryType;

    SiteType(String siteName, CountryType countryType){
        this.siteName = siteName;
        this.countryType = countryType;
    }

    public String getSiteName() {
        return siteName;
    }

    public CountryType getCountryType() {
        return countryType;
    }
}
