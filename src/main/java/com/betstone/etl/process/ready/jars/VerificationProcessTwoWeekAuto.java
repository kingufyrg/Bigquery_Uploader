package com.betstone.etl.process.ready.jars;

import com.betstone.etl.ScorecardHandler;
import com.betstone.etl.enums.ReportType;
import com.betstone.etl.enums.SiteType;
import com.betstone.etl.models.Laos;
import com.betstone.etl.models.Mexico;
import com.betstone.etl.models.Nepal;
import com.betstone.etl.models.Thurks;
import com.betstone.etl.process.ready.ProfitVerificator;

import java.time.LocalDate;

import static com.betstone.etl.io.IOUtils.getLastSunday;
import static com.betstone.etl.io.IOUtils.setPropertiesFile;

public class VerificationProcessTwoWeekAuto {
    public static void execute() {
        ProfitVerificator vsm = new ProfitVerificator();
        vsm.setScorecardHandler(new ScorecardHandler());
        setPropertiesFile("config.properties");
        vsm.setPrintWriter();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate lastWeek = LocalDate.now().minusDays(8);
        LocalDate lastSunday = getLastSunday().plusDays(1);
        LocalDate lastSunday2 = lastSunday.minusDays(7);


        ////////////////////////////////// Mexico ///////////////////////////////////////////
        Mexico mexico = new Mexico(yesterday);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                mexico, true);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                mexico, true);
        vsm.compareOneWeekValues(ReportType.MYSTERY,
                mexico, true);

        Mexico mexicoW = new Mexico(lastWeek);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                mexicoW, true);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                mexicoW, true);
        vsm.compareOneWeekValues(ReportType.MYSTERY,
                mexicoW, true);

        Mexico mexicoI = new Mexico(lastSunday);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                mexicoI, false);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                mexicoI, false);

        mexicoI = new Mexico(lastSunday2);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                mexicoI, false);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                mexicoI, false);

        //////////////////////////////////// Laos ///////////////////////////////////////////
        Laos laos = new Laos(yesterday);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                laos, true);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                laos, true);

        laos = new Laos(lastWeek);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                laos, true);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                laos, true);

        laos = new Laos(lastSunday);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                laos, false);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                laos, false);

        laos = new Laos(lastSunday2);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                laos, false);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                laos, false);


        ////////////////////////////////// Turks ///////////////////////////////////////////
        Thurks thurks = new Thurks(yesterday);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                thurks, true);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                thurks, true);

        thurks = new Thurks(lastWeek);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                thurks, true);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                thurks, true);

        thurks = new Thurks(lastSunday);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                thurks, false);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                thurks, false);

        thurks = new Thurks(lastSunday2);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                thurks, false);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                thurks, false);

        //////////////////////////////////// Nepal ///////////////////////////////////////////
        Nepal nepal = new Nepal(yesterday, SiteType.SHANGRI);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                nepal, true);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                nepal, true);

        nepal = new Nepal(lastWeek, SiteType.SHANGRI);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                nepal, true);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                nepal, true);

        nepal = new Nepal(lastSunday, SiteType.SHANGRI);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                nepal, false);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                nepal, false);

        nepal = new Nepal(lastSunday2, SiteType.SHANGRI);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                nepal, false);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                nepal, false);


        Nepal nepalIndian = new Nepal(yesterday, SiteType.TIGER_PALACE);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                nepalIndian, true);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                nepalIndian, true);

        nepalIndian = new Nepal(lastWeek, SiteType.TIGER_PALACE);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                nepalIndian, true);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                nepalIndian, true);

        nepalIndian = new Nepal(lastSunday, SiteType.TIGER_PALACE);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                nepalIndian, false);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                nepalIndian, false);

        nepalIndian = new Nepal(lastSunday2, SiteType.TIGER_PALACE);
        vsm.compareOneWeekValues(ReportType.ALL_GAME_PROFIT,
                nepalIndian, false);
        vsm.compareOneWeekValues(ReportType.SCORECARD_EGM,
                nepalIndian, false);

        vsm.getScorecardHandler().cancelProcess();
        vsm.getPrintWriter().close();
        vsm.rectificationProcess(true);
    }

    public static void main(String[] args) {
        execute();
    }


}
