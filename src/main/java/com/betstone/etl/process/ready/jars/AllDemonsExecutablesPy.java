/**package com.betstone.etl.process.ready.jars;*/

package com.betstone.etl.process.ready.jars;

import java.time.LocalDateTime;

import static com.betstone.etl.ScorecardHandler.LOGGER;
import com.betstone.etl.*;
public class AllDemonsExecutablesPy {
    public static void main(String[] args) {

            LOGGER.info("ETL diario corriendo...");
            ETLProcessOneDayAutoPy.execute();

            /**if (LocalDateTime.now().getDayOfWeek().getValue() == 1) { // Se ejecuta en cada Lunes a las 4 AM
             ETLProcessOneWeekAutoPy.execute();
             VerificationProcessOneMonthAutoPy.execute();
             } else*/
            LOGGER.info("Durmiendo: Demonio semanal");
            System.exit(0);

    }
}


/**public class AllDemonsExecutablesPy {
    public static void main(String[] args) {
        Runnable runnable = () -> {
            if (LocalDateTime.now().getHour() == 10 ) { // Se ejecuta a las 4 AM
                ETLProcessOneDayAutoPy.execute();
                if (LocalDateTime.now().getDayOfWeek().getValue() == 1) { // Se ejecuta en cada Lunes a las 4 AM
                    ETLProcessOneWeekAutoPy.execute();
                    VerificationProcessOneMonthAutoPy.execute();
                } else
                    ScorecardHandler.LOGGER.info("Durmiendo: Demonio semanal");
            } else
                ScorecardHandler.LOGGER.info("Durmiendo: Demonio diario");
        };
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.HOURS);

        Runnable runnableVTwoW = () -> {
            if (LocalDateTime.now().getDayOfWeek().getValue() % 2 == 0
                    && LocalDateTime.now().getHour() == 19) { // Se ejecuta cada dos dias, a las 7 PM
                VerificationProcessTwoWeekAutoPy.execute();
            } else
                ScorecardHandler.LOGGER.info("Durmiendo: Demonio verificador dos semanas");
        };
        ScheduledExecutorService serviceVTwoW =
                Executors.newSingleThreadScheduledExecutor();
        serviceVTwoW.scheduleAtFixedRate(runnableVTwoW, 0, 1, TimeUnit.HOURS);
    }
}*/