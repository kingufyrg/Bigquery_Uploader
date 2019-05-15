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

public class VerificationProcessOneMonthAuto {

    public static void execute() {
        ProfitVerificator vsm = new ProfitVerificator();
        vsm.setScorecardHandler(new ScorecardHandler());
        setPropertiesFile("config.properties");
        vsm.setPrintWriter();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate lastSunday = getLastSunday();

        //////////////////////////////////// Mexico ///////////////////////////////////////////
        Mexico mexico = new Mexico(yesterday);
        Mexico mexicoS = new Mexico(lastSunday);
        vsm.compareOneMonthValues(ReportType.MYSTERY,
                mexicoS, false);
        vsm.compareOneMonthValues(ReportType.ALL_GAME_PROFIT,
                mexicoS, false);
        vsm.compareOneMonthValues(ReportType.SCORECARD_EGM,
                mexicoS, false);

        vsm.compareOneMonthValues(ReportType.ALL_GAME_PROFIT,
                mexico, true);
        vsm.compareOneMonthValues(ReportType.SCORECARD_EGM,
                mexico, true);

        //////////////////////////////////// Laos ///////////////////////////////////////////
        Laos laos = new Laos(yesterday);
        Laos laosS = new Laos(lastSunday);
        vsm.compareOneMonthValues(ReportType.ALL_GAME_PROFIT,
                laosS, false);
        vsm.compareOneMonthValues(ReportType.SCORECARD_EGM,
                laosS, false);

        vsm.compareOneMonthValues(ReportType.ALL_GAME_PROFIT,
                laos, true);
        vsm.compareOneMonthValues(ReportType.SCORECARD_EGM,
                laos, true);

        //////////////////////////////////// Turks ///////////////////////////////////////////
        Thurks thurks = new Thurks(yesterday);
        Thurks thurksS = new Thurks(lastSunday);
        vsm.compareOneMonthValues(ReportType.ALL_GAME_PROFIT,
                thurksS, false);
        vsm.compareOneMonthValues(ReportType.SCORECARD_EGM,
                thurksS, false);

        vsm.compareOneMonthValues(ReportType.ALL_GAME_PROFIT,
                thurks, true);
        vsm.compareOneMonthValues(ReportType.SCORECARD_EGM,
                thurks, true);

        //////////////////////////////////// Nepal ///////////////////////////////////////////
        Nepal nepal = new Nepal(yesterday, SiteType.SHANGRI);
        Nepal nepalS = new Nepal(lastSunday, SiteType.SHANGRI);
        vsm.compareOneMonthValues(ReportType.ALL_GAME_PROFIT,
                nepalS, false);
        vsm.compareOneMonthValues(ReportType.SCORECARD_EGM,
                nepalS, false);

        vsm.compareOneMonthValues(ReportType.ALL_GAME_PROFIT,
                nepal, true);
        vsm.compareOneMonthValues(ReportType.SCORECARD_EGM,
                nepal, true);


        Nepal nepalIndian = new Nepal(yesterday, SiteType.TIGER_PALACE);
        Nepal nepalIndianS = new Nepal(lastSunday, SiteType.TIGER_PALACE);
        vsm.compareOneMonthValues(ReportType.ALL_GAME_PROFIT,
                nepalIndianS, false);
        vsm.compareOneMonthValues(ReportType.SCORECARD_EGM,
                nepalIndianS, false);

        vsm.compareOneMonthValues(ReportType.ALL_GAME_PROFIT,
                nepalIndian, true);
        vsm.compareOneMonthValues(ReportType.SCORECARD_EGM,
                nepalIndian, true);

        vsm.getScorecardHandler().cancelProcess();

        vsm.getPrintWriter().close();
        vsm.rectificationProcess(true);
    }


    public static void main(String[] args) {
        VerificationProcessOneMonthAuto.execute();
    }

}
