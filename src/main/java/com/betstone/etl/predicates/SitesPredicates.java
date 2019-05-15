package com.betstone.etl.predicates;

import com.betstone.etl.enums.SiteType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.function.Predicate;

public class SitesPredicates {
    public static Predicate<WebElement> isNotSameSitesSelected(SiteType siteType) {
            return p -> !p.getText().contains(siteType.getSiteName())
                    && p.findElement(By.cssSelector("input")).isSelected()
                    && !p.getText().contains("all");
    }

    public static Predicate<WebElement> isNotShangriSiteSelected() {
        return p ->  !p.getText().contains(SiteType.TIGER_PALACE.getSiteName())
                && p.findElement(By.cssSelector("input")).isSelected()
                && !p.getText().contains("all");
    }

    public static Predicate<WebElement> isShangriSiteSelected() {
        return p -> p.getText().contains(SiteType.SHANGRI.getSiteName())
                & p.getText().contains(SiteType.MILLIONAIRES_CLUB.getSiteName())
                && p.findElement(By.cssSelector("input")).isSelected()
                && !p.getText().contains("all");
    }
}
