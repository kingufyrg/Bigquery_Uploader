package com.betstone.etl.predicates;

import com.betstone.etl.models.Pais;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.function.Predicate;

public class CountryPredicates {

    public static Predicate<WebElement> isSameCountryAndInputsDeselected(Pais pais) {
        return p -> p.getText().contains(pais.getCountryType().getName())
                && !p.findElement(By.cssSelector("input")).isSelected();
    }

    public static Predicate<WebElement> isNotSameCountryAndInputsSelected(Pais pais){
        return p -> !p.getText().contains(pais.getCountryType().getName())
                && p.findElement(By.cssSelector("input")).isSelected();
    }
}
