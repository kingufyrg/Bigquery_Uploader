package com.betstone.etl.predicates;

import com.betstone.etl.enums.CasinoOperators;
import com.betstone.etl.models.Pais;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.function.Predicate;

public class CasinoOperatorsPredicates {

    public static Predicate<WebElement> isCasinoOperatorsFromCountryNotSelected(Pais pais) {
        return li -> CasinoOperators.getAllOperatorsFromToStringArray(pais.getCountryType())
                .contains(li.getText())
                && !li.findElement(By.cssSelector("input")).isSelected();
    }

    public static Predicate<WebElement> isCasinoOperatorsFromCountryNotSelectedFromSegment(Pais pais, int segment) {
        return li -> CasinoOperators.getAllOperatorsFromAndSameSegmentToStringArray(pais.getCountryType(),
                segment).contains(li.getText())
                && !li.findElement(By.cssSelector("input")).isSelected();
    }

    public static Predicate<WebElement> isCasinoOperatorsNotFromCountrySelected(Pais pais) {
        return li -> !CasinoOperators.getAllOperatorsFromToStringArray(pais.getCountryType()).contains(li.getText())
                && li.findElement(By.cssSelector("input")).isSelected() && li.isDisplayed();
    }

    public static Predicate<WebElement> isCasinoOperatorsNotFromCountrySelectedFromSegment(Pais pais, int segment) {
        return li -> !CasinoOperators.getAllOperatorsFromAndSameSegmentToStringArray(pais.getCountryType(), segment)
                .contains(li.getText())
                && li.findElement(By.cssSelector("input")).isSelected() && li.isDisplayed();
    }

    public static Predicate<? super WebElement> isOneOperatorsNotSelected(CasinoOperators operator) {
        return li -> li.getText().contains(operator.getName())
                && !li.findElement(By.cssSelector("input")).isSelected();
    }

    public static Predicate<WebElement> isNotOneOperatorsSelected(CasinoOperators operator) {
        return li -> !li.getText().contains(operator.getName())
                && li.findElement(By.cssSelector("input")).isSelected() && li.isDisplayed();
    }
}
