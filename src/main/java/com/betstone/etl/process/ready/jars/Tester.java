package com.betstone.etl.process.ready.jars;

import bigquery.BigQueryUploader;
import com.betstone.etl.ScorecardHandler;
import com.betstone.etl.io.IOUtils;
import com.betstone.etl.io.PythonTransformation;

import java.time.LocalDate;

public class Tester {
    public static void main(String[] args) {
        ScorecardHandler scorecardHandler = new ScorecardHandler();
        IOUtils.setPropertiesFile("config.properties");
        LocalDate yesterday = LocalDate.now().minusDays(1);
        scorecardHandler.setDaily(true);
        scorecardHandler.useFormatExcel(false);

        //////////////////////////////////// Mexico ///////////////////////////////////////////
//        Mexico mexico = new Mexico(yesterday);
//        scorecardHandler.oneDayDownloadCountry(
//                ReportType.ALL_GAME_PROFIT,
//                mexico);
//        scorecardHandler.oneDayDownloadCountry(
//                ReportType.SCORECARD_EGM,
//                mexico);
//        scorecardHandler.oneDayDownloadCountry(
//                ReportType.MYSTERY,
//                mexico);

//        scorecardHandler.cancelProcess();
        ScorecardHandler.LOGGER.info("\t----------- \t-----------");
        ScorecardHandler.LOGGER.info("Extracción completada, iniciando proceso de Transformación");

        PythonTransformation python = new PythonTransformation();
        python.execute();

        ScorecardHandler.LOGGER.info("\t----------- \t-----------");
        ScorecardHandler.LOGGER.info("Proceso de Transformación completado, iniciando proceso de Subida");
        BigQueryUploader bigQueryUploader = new BigQueryUploader();
        bigQueryUploader.uploadBigQueryFiles();

        ScorecardHandler.LOGGER.info("\t----------- \t-----------");
        ScorecardHandler.LOGGER.info("Proceso para dia: " + yesterday.toString() + " completado satisfactoriamente.");
        ScorecardHandler.LOGGER.info("\t----------- \t-----------");

    }
}
