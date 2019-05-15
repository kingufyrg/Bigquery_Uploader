package com.betstone.etl.process.ready.jars;

import bigquery.BigQueryUploader;
import com.betstone.etl.ScorecardHandler;
import com.betstone.etl.enums.ReportType;
import com.betstone.etl.enums.SiteType;
import com.betstone.etl.io.IOUtils;
import com.betstone.etl.models.Laos;
import com.betstone.etl.models.Mexico;
import com.betstone.etl.models.Nepal;
import com.betstone.etl.models.Thurks;

import java.time.LocalDate;

public class ETLProcessOneDayAuto {

    public static void execute() {
        ScorecardHandler scorecardHandler = new ScorecardHandler();
        IOUtils.setPropertiesFile("config.properties");
        /**LocalDate yesterday = LocalDate.now().minusDays(1);*/
        LocalDate yesterday = LocalDate.now();
        scorecardHandler.setDaily(true);
        scorecardHandler.useFormatExcel(true);

        //////////////////////////////////// Mexico ///////////////////////////////////////////
        Mexico mexico = new Mexico(yesterday);
        scorecardHandler.oneDayDownloadCountry(
                ReportType.ALL_GAME_PROFIT,
                mexico);
        scorecardHandler.oneDayDownloadCountry(
                ReportType.SCORECARD_EGM,
                mexico);
        scorecardHandler.oneDayDownloadCountry(
                ReportType.MYSTERY,
                mexico);

        //////////////////////////////////// Laos ///////////////////////////////////////////
        Laos laos = new Laos(yesterday);
        scorecardHandler.oneDayDownloadCountry(
                ReportType.ALL_GAME_PROFIT,
                laos);
        scorecardHandler.oneDayDownloadCountry(
                ReportType.SCORECARD_EGM,
                laos);
        scorecardHandler.oneDayDownloadCountry(
                ReportType.MYSTERY,
                laos);


        //////////////////////////////////// Turks ///////////////////////////////////////////
        Thurks thurks = new Thurks(yesterday);
        scorecardHandler.oneDayDownloadCountry(
                ReportType.ALL_GAME_PROFIT,
                thurks);
        scorecardHandler.oneDayDownloadCountry(
                ReportType.SCORECARD_EGM,
                thurks);
        scorecardHandler.oneDayDownloadCountry(
                ReportType.MYSTERY,
                thurks);


        //////////////////////////////////// Nepal ///////////////////////////////////////////
        Nepal nepal = new Nepal(yesterday, SiteType.SHANGRI);
        scorecardHandler.oneDayDownloadCountry(
                ReportType.ALL_GAME_PROFIT,
                nepal);
        scorecardHandler.oneDayDownloadCountry(
                ReportType.SCORECARD_EGM,
                nepal);
        scorecardHandler.oneDayDownloadCountry(
                ReportType.MYSTERY,
                nepal);

        Nepal nepalIndian = new Nepal(yesterday, SiteType.TIGER_PALACE);
        scorecardHandler.oneDayDownloadCountry(
                ReportType.ALL_GAME_PROFIT,
                nepalIndian);
        scorecardHandler.oneDayDownloadCountry(
                ReportType.SCORECARD_EGM,
                nepalIndian);
        scorecardHandler.oneDayDownloadCountry(
                ReportType.MYSTERY,
                nepalIndian);

        scorecardHandler.cancelProcess();
        ScorecardHandler.LOGGER.info("\t----------- \t-----------");
        ScorecardHandler.LOGGER.info("Extracción completada, iniciando proceso de Transformación");

        MacroExecuter macroExecuter = new MacroExecuter();
        macroExecuter.execute(scorecardHandler);

        ScorecardHandler.LOGGER.info("\t----------- \t-----------");
        ScorecardHandler.LOGGER.info("Proceso de Transformación completado, iniciando proceso de Subida");
        BigQueryUploader bigQueryUploader = new BigQueryUploader();
        bigQueryUploader.uploadBigQueryFiles();

        ScorecardHandler.LOGGER.info("\t----------- \t-----------");
        ScorecardHandler.LOGGER.info("Proceso para dia: " + yesterday.toString() + " completado satisfactoriamente.");
        ScorecardHandler.LOGGER.info("\t----------- \t-----------");
    }
}
