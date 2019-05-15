package com.betstone.etl.process.ready;

import bigquery.BigQueryConsultor;
import bigquery.BigQueryUploader;
import com.betstone.etl.ScorecardHandler;
import com.betstone.etl.enums.ReportType;
import com.betstone.etl.enums.SiteType;
import com.betstone.etl.io.IOUtils;
import com.betstone.etl.io.PythonTransformation;
import com.betstone.etl.models.*;
import com.betstone.etl.process.ready.jars.MacroExecuter;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.betstone.etl.io.IOUtils.scorecardFormat;

public class ProfitVerificator {
    ScorecardHandler scorecardHandler;

    String totalInCellId = scorecardHandler.totalInCellId;
    String totalOutCellId = scorecardHandler.totalOutCellId;
    String ggrProfit = scorecardHandler.ggrProfit;
    String ggrCellId = scorecardHandler.ggrCellId + " span";
    private PrintWriter printWriter;
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;


    /*
    El proceso de verificación habría de ser de la siguiente forma para el caso diario:
    1. Se hace la carga de Scorecard con el Pais X, Reporte Y y valor a Z a buscar para confirmar.
    1.1 Se hace la búsqueda del elemento web que contiene el resultado (y que habría de ser inmutable en su referencia).
    1.2 Se guarda el valpr
    2. Se hace un query solicitando sólo el valor que uno espera confirmar su validez desde BigQuery.
    2.1 Se hace conexión a BigQuery del proyecto X, el data scource Y y la tabla Z que contiene el valor a buscar.
    2.2 Se ejecuta un query preparado (para evitar procesamiento local) que calcule la cantidad buscada.
    3. Se confirman si las cantidades corresponden.
        Sí: Continúa al siguiente día, regresando al paso 1.
        No: Se detiene el proceso indicando el día no correspondiente. <Pudiera implementarse una continunación del proceso
        sólo marcando los días que no corresponden>
     */


    public <T extends Pais> void compareOneWeekValues(ReportType reportType, T t, boolean daily) {
        LocalDate initDay = t.getFecha().minusDays(6),
                finalDay = t.getFecha();
        t.setFecha(initDay);
        compareAndPrintForWeek(reportType, t, printWriter, finalDay, daily);
    }

    private <T extends Pais> void compareAndPrintForWeek(ReportType reportType, T t, PrintWriter printWriter,
                                                         LocalDate finalDay, boolean daily) {
        while (!t.getFecha().isEqual(finalDay.plusDays(1))) {
            boolean result = compareOneDayValues(reportType,
                    t, daily);
            if (!result) {
                printToFile(reportType, t, printWriter, daily);
            }
            t.setFecha(t.getFecha().plusDays(1));
        }
        t.setFecha(finalDay);
    }

    private <T extends Pais> void printToFile(ReportType reportType, T t, PrintWriter printWriter, boolean daily) {
        printWriter.println(((t instanceof Nepal)
                ? t.getCountryType().getName() + "_" + ((Nepal) t).getSiteType()
                : t.getCountryType().getName())
                + "," + t.getFecha().toString()
                + "," + reportType.name() + "," + daily);
    }

    public <T extends Pais> void compareOneMonthValues(ReportType reportType, T t, boolean daily) {
        LocalDate initDay = t.getFecha().minusDays(30),
                finalDay = t.getFecha();
        t.setFecha(initDay);
        compareAndPrintForWeek(reportType, t, printWriter, finalDay, daily);
    }

    public <T extends Pais> boolean compareOneDayValues(ReportType reportType, T t, boolean daily) {
        ScorecardHandler.LOGGER.info("---------------- \tPaís: "
                + t.getCountryType().name() + "\t-----------------------");
        ScorecardHandler.LOGGER.info("---------------- \tDía: "
                + IOUtils.getDateFormatted(t.getFecha(), scorecardFormat) + "-----------------------");
        ScorecardHandler.LOGGER.info("---------------- \tReporte: "
                + reportType.name() + ", " + daily + "\t-----------------------");
        if (reportType == ReportType.MYSTERY && !(t instanceof Mexico))
            return true;
        List<Double> valuesFromScorecard =
                fillScoreCardValues(reportType, t);
        if(!(fillScoreCardValues(reportType,t)==null)){
        ScorecardHandler.LOGGER.info("Cantidades obtenidas de Scorecard: \t" + Arrays.toString(valuesFromScorecard.toArray()));
        List<Double> valuesFromBigQuery;
        BigQueryConsultor bigQueryConn = new BigQueryConsultor();
        bigQueryConn.setDaily(daily);

        try {
            valuesFromBigQuery = bigQueryConn.returnDoubleValuesAsListFromReport(reportType, t);
            ScorecardHandler.LOGGER.info("Cantidades obtenidas de BigQuery: \t" + Arrays.toString(valuesFromBigQuery.toArray()));
            boolean match = allMatch(valuesFromScorecard, valuesFromBigQuery);
            ScorecardHandler.LOGGER.info("Match: " + match);
            return match;
        } catch (Exception e) {
            ScorecardHandler.LOGGER.fatal(e.getMessage());
        }}
        else{return true;}
        return false;
    }

    public void oneDayVerificationList(List<Pais> paisList, List<ReportType> reportTypes, boolean daily) {
        scorecardHandler = new ScorecardHandler();
        Stream<Pais> paisStream = paisList.stream();
        try {
            paisStream.forEach(pais -> reportTypes.stream()
                    .forEach(reportType -> {
                        if (!compareOneDayValues(reportType, pais, daily)) {
                            printToFile(reportType, pais, printWriter, daily);
                        }
                    }));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scorecardHandler.cancelProcess();
        }
    }

    public void oneWeekVerificationList(List<Pais> paisList, List<ReportType> reportTypes, boolean daily) {
        scorecardHandler = new ScorecardHandler();
        Stream<Pais> paisStream = paisList.stream();
        try {
            paisStream.forEach(pais -> reportTypes.stream()
                    .forEach(reportType -> compareOneWeekValues(reportType, pais, daily)));
        } catch (Exception e) {
            ScorecardHandler.LOGGER.fatal(e.getMessage());
        } finally {
            scorecardHandler.cancelProcess();
        }
    }

    public void oneMonthVerificationList(List<Pais> paisList, List<ReportType> reportTypes, boolean daily) {
        scorecardHandler = new ScorecardHandler();
        Stream<Pais> paisStream = paisList.stream();
        try {
            paisStream.forEach(pais -> reportTypes.stream()
                    .forEach(reportType -> compareOneMonthValues(reportType, pais, daily)));
        } catch (Exception e) {
            ScorecardHandler.LOGGER.fatal(e.getMessage());
        } finally {
            scorecardHandler.cancelProcess();
        }
    }


    private <T extends Pais> List<Double> fillScoreCardValues(ReportType reportType, T t) {

        List<Double> valuesFromScorecard = getDayProfitNoAccumulative(reportType, t);
        return valuesFromScorecard;

    }

    private boolean allMatch(List<Double> valuesFromScorecard, List<Double> valuesFromBigQuery) {
        boolean veredict = true;
        for (Double value : valuesFromBigQuery) {
            veredict = veredict && valuesFromScorecard.contains(value);
        }
        if(valuesFromScorecard.isEmpty()){veredict=true;}
        return veredict;
    }


    private <T extends Pais> List<Double> getDayProfitNoAccumulative(ReportType reportType, T t) {
        if (reportType != ReportType.MYSTERY)
            return scorecardHandler.profitFromOneDayOneCountry(reportType, t, totalInCellId, totalOutCellId, ggrProfit);
        else return scorecardHandler.profitFromOneDayOneCountry(reportType, t, ggrCellId);
    }

    public void rectificationProcess(boolean formatExcel) {
        try {
            ScorecardHandler.LOGGER.info("\t------------- \tIniciando Proceso de Rectificación \t-------------");
            List<String> lines = Files.readAllLines(Paths.get("resultsBadDays.txt"), Charset.defaultCharset());
            ScorecardHandler.LOGGER.info("Archivo de texto leído. Procesos a realizar: " + lines.size());
            List<String[]> linesMapped = lines.stream()
                    .map(s -> s.split(","))
                    .collect(Collectors.toList());
            if (scorecardHandler == null)
                scorecardHandler = new ScorecardHandler();
            BigQueryConsultor retriever = new BigQueryConsultor();
            BigQueryUploader uploader = new BigQueryUploader();
            linesMapped.stream()
                    .forEach(l -> {
                        ReportType report = getReportTypeFromText(l[2]);
                        Pais pais = getCountryFromText(l[0], l[1]);
                        boolean daily = isDailyOnText(l[3]);
                        retriever.setDaily(daily);
                        scorecardHandler.useFormatExcel(formatExcel);
                        ScorecardHandler.LOGGER.info("--------- \tRectificación para: " + pais.getCountryType().getName()
                                + " con día: " + pais.getFecha() + " en reporte: " + report.name()
                                + ", daily: " + daily);
                        ScorecardHandler.LOGGER.info("Borrando archivos correspondientes de BigQuery");
                        retriever.deleteOneDayDataOneCountry(report, pais);
                        ScorecardHandler.LOGGER.info("Descargando archivos correspondientes de Scorecard.");
                        scorecardHandler.setDaily(daily);
                        scorecardHandler.oneDayDownloadCountry(report, pais);
                        scorecardHandler.cancelProcess();
                    });
            ScorecardHandler.LOGGER.info("Iniciando macro.");
            if (!formatExcel) {
                PythonTransformation py = new PythonTransformation();
                py.execute();
            } else {
                MacroExecuter executer = new MacroExecuter();
                executer.execute(getScorecardHandler());
            }
            ScorecardHandler.LOGGER.info("Subiendo archivos a BigQuery.");
            uploader.uploadBigQueryFiles();
            Files.delete(Paths.get("resultsBadDays.txt"));
            ScorecardHandler.LOGGER.info("Archivo de texto borrado");
        } catch (IOException e) {
            ScorecardHandler.LOGGER.fatal("Error de I/O");
        }
    }


    private boolean isDailyOnText(String s) {
        return s.equalsIgnoreCase("true");
    }

    private Pais getCountryFromText(String country, String date) {
        switch (country) {
            case "Mexico":
                return new Mexico(stringToDate(date));
            case "Laos":
                return new Laos(stringToDate(date));
            case "Nepal_TIGER_PALACE":
                return new Nepal(stringToDate(date), SiteType.TIGER_PALACE);
            case "Nepal_SHANGRI":
                return new Nepal(stringToDate(date), SiteType.SHANGRI);
            case "Turks":
                return new Thurks(stringToDate(date));
            case "Spain":
                return new Spain(stringToDate(date));
        }
        return null;
    }

    private LocalDate stringToDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private ReportType getReportTypeFromText(String string) {
        switch (string) {
            case "ALL_GAME_PROFIT":
                return ReportType.ALL_GAME_PROFIT;
            case "SCORECARD_EGM":
                return ReportType.SCORECARD_EGM;
            case "MYSTERY":
                return ReportType.MYSTERY;
        }
        return null;
    }

    public ScorecardHandler getScorecardHandler() {
        return scorecardHandler;
    }

    public void setScorecardHandler(ScorecardHandler scorecardHandler) {
        this.scorecardHandler = scorecardHandler;
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    public void setPrintWriter() {
        try {
            this.fileWriter = new FileWriter("resultsBadDays.txt", true);
            this.bufferedWriter = new BufferedWriter(this.fileWriter);
            this.printWriter = new PrintWriter(this.bufferedWriter, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
