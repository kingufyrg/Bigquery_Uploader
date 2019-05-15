package com.betstone.etl.enums;

public enum CountryType {
    MEXICO("Mexico"),  LAOS("Laos"), NEPAL("Nepal"), SPAIN("Spain"), THURKS("Turks");

    private final String name;

    CountryType(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
