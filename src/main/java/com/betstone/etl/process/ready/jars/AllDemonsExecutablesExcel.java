package com.betstone.etl.process.ready.jars;

import com.betstone.etl.ScorecardHandler;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AllDemonsExecutablesExcel {
    public static void main(String[] args) {
        Runnable runnable = () -> {
            if (LocalDateTime.now().getHour() ==19  ) {
                ETLProcessOneDayAuto.execute();
                if (LocalDateTime.now().getDayOfWeek().getValue() == 1) {
                    ETLProcessOneWeekAuto.execute();
                    /**VerificationProcessOneMonthAuto.execute();*/
                } else
                    ScorecardHandler.LOGGER.info("Durmiendo: Demonio semanal");
            } else
                ScorecardHandler.LOGGER.info("Durmiendo: Demonio diario");
        };
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.HOURS);
/**
        Runnable runnableVTwoW = () -> {
            if (LocalDateTime.now().getDayOfWeek().getValue() % 2 == 0
                    && LocalDateTime.now().getHour() == 4) {
                VerificationProcessTwoWeekAuto.execute();
            } else
                ScorecardHandler.LOGGER.info("Durmiendo: Demonio verificador dos semanas");
        };
        ScheduledExecutorService serviceVTwoW =
                Executors.newSingleThreadScheduledExecutor();
        serviceVTwoW.scheduleAtFixedRate(runnableVTwoW, 0, 1, TimeUnit.HOURS);
    */
    }
}
