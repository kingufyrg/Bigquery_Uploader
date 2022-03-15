package com.betstone.etl.process.ready.jars;

import bigquery.BigQueryUploader;
import com.betstone.etl.ScorecardHandler;
import com.betstone.etl.enums.ReportType;
import com.betstone.etl.enums.SiteType;
import com.betstone.etl.io.IOUtils;
import com.betstone.etl.io.PythonTransformation;
import com.betstone.etl.models.Laos;
import com.betstone.etl.models.Mexico;
import com.betstone.etl.models.Nepal;
import com.betstone.etl.models.Thurks;

import java.time.LocalDate;

public class ETLProcessOneWeekAutoPy {

    public static void execute() {
        ScorecardHandler scorecardHandler = new ScorecardHandler();
        IOUtils.setPropertiesFile("config.properties");

        LocalDate yesterday = LocalDate.now();
        scorecardHandler.setDaily(false);
        scorecardHandler.useFormatExcel(true);

        Mexico mexico = new Mexico(yesterday);
        scorecardHandler.oneWeekDownloadCountry(
                ReportType.ALL_GAME_PROFIT,
                mexico);
        scorecardHandler.oneWeekDownloadCountry(
                ReportType.SCORECARD_EGM,
                mexico);
        scorecardHandler.oneWeekDownloadCountry(
                ReportType.MYSTERY,
                mexico);


        Laos laos = new Laos(yesterday);
        scorecardHandler.oneWeekDownloadCountry(
                ReportType.ALL_GAME_PROFIT,
                laos
        );
        scorecardHandler.oneWeekDownloadCountry(
                ReportType.SCORECARD_EGM,
                laos);
        scorecardHandler.oneWeekDownloadCountry(
                ReportType.MYSTERY,
                laos);


        Thurks thurks = new Thurks(yesterday);
        scorecardHandler.oneWeekDownloadCountry(
                ReportType.ALL_GAME_PROFIT,
                thurks);
        scorecardHandler.oneWeekDownloadCountry(
                ReportType.SCORECARD_EGM,
                thurks);
        scorecardHandler.oneWeekDownloadCountry(
                ReportType.MYSTERY,
                thurks);

        Nepal nepal = new Nepal(yesterday, SiteType.SHANGRI);
        scorecardHandler.oneWeekDownloadCountry(
                ReportType.ALL_GAME_PROFIT,
                nepal);
        scorecardHandler.oneWeekDownloadCountry(
                ReportType.SCORECARD_EGM,
                nepal);
        scorecardHandler.oneWeekDownloadCountry(
                ReportType.MYSTERY,
                nepal);


        Nepal nepalIndian = new Nepal(yesterday, SiteType.TIGER_PALACE);
        scorecardHandler.oneWeekDownloadCountry(
                ReportType.ALL_GAME_PROFIT,
                nepalIndian);
        scorecardHandler.oneWeekDownloadCountry(
                ReportType.SCORECARD_EGM,
                nepalIndian);
        scorecardHandler.oneWeekDownloadCountry(
                ReportType.MYSTERY,
                nepalIndian);

        scorecardHandler.cancelProcess();
        ScorecardHandler.LOGGER.info("\t----------- \t-----------");
        ScorecardHandler.LOGGER.info("Extracción completada, iniciando proceso de Transformación");

        PythonTransformation py = new PythonTransformation();
        py.execute();

        ScorecardHandler.LOGGER.info("\t----------- \t-----------");
        ScorecardHandler.LOGGER.info("Proceso de Transformación completado, iniciando proceso de Subida");
        BigQueryUploader bigQueryUploader = new BigQueryUploader();
        bigQueryUploader.uploadBigQueryFiles();

        ScorecardHandler.LOGGER.info("\t----------- \t-----------");
        ScorecardHandler.LOGGER.info("Proceso para dia: " + yesterday.toString() + " completado satisfactoriamente.");
        ScorecardHandler.LOGGER.info("\t----------- \t-----------");
    }

}
