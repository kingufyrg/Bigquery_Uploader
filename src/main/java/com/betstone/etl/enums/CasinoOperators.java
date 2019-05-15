package com.betstone.etl.enums;

import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum CasinoOperators {
    AEVSA("AEVSA", CountryType.MEXICO, 1),
    GRUPO_ORENES("Grupo Orenes", CountryType.MEXICO, 1),
    GRUPO_PALERMO("Grupo Palermo", CountryType.MEXICO, 1),
    GRUPO_WIN("Grupo Win", CountryType.MEXICO, 1),
    MADEIRA_GAMING("Madeira Gaming", CountryType.MEXICO, 1),
    MAJESTIC_GROUP("Majestic Group", CountryType.MEXICO, 1),
    OPERADORA_CLASS("Operadora Class", CountryType.MEXICO, 1),
    OPERADORA_APUESTAS_MEXICO("Operadora de Apuestas México", CountryType.MEXICO, 1),
    OPERADORA_SUEÑOS("Operadora de Sueños", CountryType.MEXICO, 1),
    BGE("BGE", CountryType.MEXICO, 1),
    PETATE("Petate", CountryType.MEXICO, 1),
    PETOLOF("Petolof", CountryType.MEXICO, 1),
    PLAY_CITY_TELEVISA("Televisa", CountryType.MEXICO, 2),
    TOP_GAMBLING("Top Gambling", CountryType.MEXICO, 2),
    WINPOT("Winpot", CountryType.MEXICO, 2),
    CALIENTE("Caliente", CountryType.MEXICO, 2),
    CASINO_CENTRAL("Casino Central", CountryType.MEXICO, 2),
    CENTRAL_GAMING("Central Gaming (Royale)", CountryType.MEXICO, 2),
    CIRSA("Cirsa", CountryType.MEXICO, 2),
    ENTRETENIMIENTO_MAPUCHE("Entretenimiento Mapuche", CountryType.MEXICO, 2),
    GOC("GOC", CountryType.MEXICO, 2),
    COINCIDENCIAS("Coincidencias Númericas", CountryType.MEXICO, 2),
    GRUPO_HOUSE("Grupo House", CountryType.MEXICO, 2),
    GRUPO_FUSION("Grupo Fusion", CountryType.MEXICO, 2),
    ENTRETENIMIENTO_DEL_SUR("Entretenimiento del Sur", CountryType.MEXICO, 2),
    CARIB_GAMING("Carib Gaming", CountryType.THURKS, 1),
    SILVER_HERITAGE_LAOS("Silver Heritage", CountryType.LAOS, 1),
    SILVER_HERITAGE_NEPAL("Silver Heritage", CountryType.NEPAL, 1),
    VIDEMUR("Videmur", CountryType.SPAIN, 1);


    private final String name;
    private final CountryType countryType;
    private final int segment;

    CasinoOperators(String name, CountryType countryType, int segment) {
        this.name = name;
        this.countryType = countryType;
        this.segment = segment;
    }

    public static List<CasinoOperators> getAllOperatorsFrom(CountryType countryType) {
        ArrayList<CasinoOperators> casinoOpCountry = Arrays.asList(CasinoOperators.values()).stream()
                .filter(e -> e.countryType == countryType)
                .collect(Collectors.toCollection(ArrayList::new));
        return casinoOpCountry;
    }

    public static List<String> getAllOperatorsFromToStringArray(CountryType countryType) {
        ArrayList<String> casinoOpCountryStrings =
                Arrays.asList(CasinoOperators.values()).stream()
                        .filter(e -> e.countryType == countryType)
                        .map(op -> op.name)
                        .collect(Collectors.toCollection(ArrayList::new));
        return casinoOpCountryStrings;
    }

    public static List<String> getAllOperatorsFromAndSameSegmentToStringArray(CountryType countryType, int segment) {
        ArrayList<String> casinoOpCountryStrings =
                Arrays.asList(CasinoOperators.values()).stream()
                        .filter(e -> e.countryType == countryType && e.segment == segment)
                        .map(op -> op.name)
                        .collect(Collectors.toCollection(ArrayList::new));
        return casinoOpCountryStrings;
    }

    public static List<CasinoOperators> getEnumsFromArray(ObservableList<String> checkedItems) {
        List<CasinoOperators> operators = new ArrayList<>();
        checkedItems.stream()
                .forEach(item -> {
                    CasinoOperators[] casinos = CasinoOperators.values();
                    for (int x = 0; x <= casinos.length; x++) {
                        if (item.contains(casinos[x].getName()))
                            operators.add(casinos[x]);
                    }
                });
        return operators;
    }

    public static int getSegmentSizeFromCountry(CountryType country) {
        return getAllOperatorsFrom(country).stream()
                .mapToInt(cp -> cp.getSegment())
                .max()
                .getAsInt();
    }

    public String getName() {
        return this.name;
    }

    public CountryType getCountryType() {
        return countryType;
    }

    public int getSegment() {
        return segment;
    }
}
