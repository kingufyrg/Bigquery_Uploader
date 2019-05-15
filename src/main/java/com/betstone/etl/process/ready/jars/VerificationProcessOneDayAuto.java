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
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.betstone.etl.io.IOUtils.setPropertiesFile;

public class VerificationProcessOneDayAuto {

    private static void execute(){
        ProfitVerificator vsm = new ProfitVerificator();
        vsm.setScorecardHandler(new ScorecardHandler());
        setPropertiesFile("config.properties");
        LocalDate yesterday = LocalDate.now().minusDays(1);

        //////////////////////////////////// Mexico ///////////////////////////////////////////
        Mexico mexico = new Mexico(yesterday);
        vsm.compareOneDayValues(ReportType.ALL_GAME_PROFIT,
                mexico, true);
        vsm.compareOneDayValues(ReportType.SCORECARD_EGM,
                mexico, true);
        vsm.compareOneDayValues(ReportType.MYSTERY,
                mexico, true);

        //////////////////////////////////// Laos ///////////////////////////////////////////
        Laos laos = new Laos(yesterday);
        vsm.compareOneDayValues(ReportType.ALL_GAME_PROFIT,
                laos, true);
        vsm.compareOneDayValues(ReportType.SCORECARD_EGM,
                laos, true);

        //////////////////////////////////// Turks ///////////////////////////////////////////
        Thurks thurks = new Thurks(yesterday);
        vsm.compareOneDayValues(ReportType.ALL_GAME_PROFIT,
                thurks, true);
        vsm.compareOneDayValues(ReportType.SCORECARD_EGM,
                thurks, true);

        //////////////////////////////////// Nepal ///////////////////////////////////////////
        Nepal nepal = new Nepal(yesterday, SiteType.SHANGRI);
        vsm.compareOneDayValues(ReportType.ALL_GAME_PROFIT,
                nepal, true);
        vsm.compareOneDayValues(ReportType.SCORECARD_EGM,
                nepal, true);


        Nepal nepalIndian = new Nepal(yesterday, SiteType.TIGER_PALACE);
        vsm.compareOneDayValues(ReportType.ALL_GAME_PROFIT,
                nepalIndian, true);
        vsm.compareOneDayValues(ReportType.SCORECARD_EGM,
                nepalIndian, true);
    }

    public static void main(String[] args) {
        Runnable runnable = () -> {
            if (LocalDateTime.now().getHour() == 4)
                execute();
            else
                ScorecardHandler.LOGGER.info("Durmiendo... ");
        };
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.HOURS);
    }
}
