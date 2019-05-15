package bigquery;

import com.betstone.etl.ScorecardHandler;
import com.betstone.etl.enums.ReportType;
import com.betstone.etl.enums.SiteType;
import com.betstone.etl.models.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static bigquery.BigQueryUploader.openPropertiesFileConnection;

public class BigQueryConsultor {

    private BigQuery bigquery;
    private Properties properties;
    private final String propertiesFile = "config.properties";
    private Path credentialPath;
    private GoogleCredentials credentials;
    private boolean daily;

    public BigQueryConsultor() {
        try {
            if (getPropertiesValue("credentials.resource").equalsIgnoreCase("1")) {
                Object ob = ClassLoader.getSystemResource(getPropertiesValue("credentials.bigquery")).getContent();
                credentials = ServiceAccountCredentials.fromStream((InputStream) ob);
            } else {
                credentialPath = Paths.get(getPropertiesValue("credentials.bigquery"));
                File credentialsJson = credentialPath.toFile();
                FileInputStream serviceAccountStream = new FileInputStream(credentialsJson);
                credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
            }
            this.bigquery = BigQueryOptions
                    .newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T extends Pais> List<Double> returnDoubleValuesAsListFromReport(ReportType reportType, T t) throws Exception {
        String query = getSelectQueryByReportAndCountry(reportType, t);
        QueryJobConfiguration queryJobConfiguration =
                QueryJobConfiguration.newBuilder(query)
                        .setUseLegacySql(false)
                        .build();
        JobId jobId = JobId.of(UUID.randomUUID().toString());
        Job queryJob = bigquery.create(
                JobInfo.newBuilder(queryJobConfiguration)
                        .setJobId(jobId)
                        .build()
        );
        queryJob = queryJob.waitFor();
        if (queryJob == null)
            throw new RuntimeException("Job no longer exists");
        else if (queryJob.getStatus().getError() != null)
            throw new RuntimeException(queryJob.getStatus().getError().toString());


        TableResult result = queryJob.getQueryResults();
        List<Double> values = new ArrayList();
        result.iterateAll().forEach(fieldValues ->
                fieldValues.stream()
                        .forEach(fieldValue -> values.add(fieldValue.getDoubleValue())));
        return values;
    }

    private <T extends Pais> String getSelectQueryByReportAndCountry(ReportType reportType, T t) {
        String deviceRegex = getDeviceRegex(reportType, t);
        switch (reportType) {
            case ALL_GAME_PROFIT:
                return "SELECT " +
                        " format(\"%.2f\", sum(totalBet)) as TotalWager," +
                        " format(\"%.2f\", sum(totalWin)) as NetWIn" +
                        " FROM " +
                        ((!daily) ? "`dgc_dna_betstone.EGMGameProfit_Invoicing`" : "`dgc_dna_betstone.EGMGameProfit`") +
                        " where EGM " + deviceRegex +
                        " and extract(year from profitDate) = " + t.getFecha().getYear() +
                        " and extract(month from profitDate) = " + t.getFecha().getMonthValue() +
                        " and extract(day from profitDate) = " + t.getFecha().getDayOfMonth();
            case SCORECARD_EGM:
                return "SELECT" +
                        " format(\"%.2f\", sum(wagerAmount)) as wagerAmount," +
                        " format(\"%.2f\", sum(payoutAmount)) as payoutAmount, " +
                        " format(\"%.2f\", sum(GGR)) as GGR" +
                        " FROM " +
                        ((!daily) ? "`dgc_dna_betstone.ScorecardEGM2_Invoicing`" : "`dgc_dna_betstone.ScorecardEGM2`") +
                        " WHERE" +
                        " device " + deviceRegex +
                        " and extract(year from aggDate) = " + t.getFecha().getYear() +
                        " and extract(month from aggDate) = " + t.getFecha().getMonthValue() +
                        " and extract(day from aggDate) = " + t.getFecha().getDayOfMonth();
            case MYSTERY:
                return "SELECT" +
                        " format(\"%.2f\", sum(Bonus_Win_Amount)) as Bonus_Win_Amount" +
                        " FROM `dgc_dna_betstone.MysteryEGM` " +
                        " WHERE" +
                        " EGM " + deviceRegex +
                        " and extract(year from Date) = " + t.getFecha().getYear() +
                        " and extract(month from Date) = " + t.getFecha().getMonthValue() +
                        " and extract(day from Date) = " + t.getFecha().getDayOfMonth();
        }
        return null;
    }

    private <T extends Pais> List<String> getDeleteQueryByReportAndCountry(ReportType reportType, T t) {
        String deviceRegex = getDeviceRegex(reportType, t);
        switch (reportType) {
            case ALL_GAME_PROFIT:
                return Arrays.asList("DELETE " +
                        " FROM " + ((daily) ? "`dgc_dna_betstone.EGMGameProfit`" : "`dgc_dna_betstone.EGMGameProfit_Invoicing`") +
                        " where EGM " + deviceRegex +
                        " and extract(year from profitDate) = " + t.getFecha().getYear() +
                        " and extract(month from profitDate) = " + t.getFecha().getMonthValue() +
                        " and extract(day from profitDate) = " + t.getFecha().getDayOfMonth());
            case SCORECARD_EGM:
                return Arrays.asList("DELETE" +
                                " FROM " + ((daily) ? "`dgc_dna_betstone.ScorecardEGM2`" :
                                "`dgc_dna_betstone.ScorecardEGM2_Invoicing`") +
                        " WHERE" +
                                " device " + deviceRegex +
                                " and extract(year from aggDate) = " + t.getFecha().getYear() +
                                " and extract(month from aggDate) = " + t.getFecha().getMonthValue() +
                                " and extract(day from aggDate) = " + t.getFecha().getDayOfMonth(),
                        ("DELETE" +
                                " FROM " + ((daily) ? "`dgc_dna_betstone.AssetsDaily2`" :
                                "`dgc_dna_betstone.AssetsDaily2_Invoicing`") +
                                " WHERE" +
                                " EGM " + deviceRegex +
                                " and extract(year from aggDate) = " + t.getFecha().getYear() +
                                " and extract(month from aggDate) = " + t.getFecha().getMonthValue() +
                                " and extract(day from aggDate) = " + t.getFecha().getDayOfMonth())
                                .replaceAll("device", "EGM"));
            case MYSTERY:
                return Arrays.asList("DELETE" +
                        " FROM `dgc_dna_betstone.MysteryEGM` " +
                        " WHERE" +
                        " EGM " + deviceRegex +
                        " and extract(year from Date) = " + t.getFecha().getYear() +
                        " and extract(month from Date) = " + t.getFecha().getMonthValue() +
                        " and extract(day from Date) = " + t.getFecha().getDayOfMonth());
        }
        return null;
    }

    private <T extends Pais> String getDeviceRegex(ReportType reportType, T t) {
        if (t instanceof Mexico)
            return "like 'BMX%'";
        else if (t instanceof Laos)
            return "like 'BLA%'";
        else if (t instanceof Nepal) {
            if (((Nepal) t).getSiteType() == SiteType.SHANGRI)
                return "like 'BNPSHG%'";
            else
                return "not like 'BNPSHG%' and " +
                        ((reportType == ReportType.ALL_GAME_PROFIT) ?
                                "EGM like 'BNP%'" :
                                "device like 'BNP%'");
        } else if (t instanceof Thurks)
            return "like 'BTC%'";
        return null;
    }

    public String getPropertiesValue(String property) throws IOException {
        properties = new Properties();
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(propertiesFile);
        return openPropertiesFileConnection(property, inputStream, properties, propertiesFile);
    }

    public boolean deleteOneDayDataOneCountry(ReportType report, Pais pais) {
        try {
            List<String> queries = getDeleteQueryByReportAndCountry(report, pais);
            for (String query : queries) {
                QueryJobConfiguration queryJobConfiguration =
                        QueryJobConfiguration.newBuilder(query)
                                .setUseLegacySql(false)
                                .build();
                JobId jobId = JobId.of(UUID.randomUUID().toString());
                Job queryJob = bigquery.create(
                        JobInfo.newBuilder(queryJobConfiguration)
                                .setJobId(jobId)
                                .build()
                );
                queryJob = queryJob.waitFor();
                if (queryJob == null)
                    throw new RuntimeException("Job no longer exists");
                else if (queryJob.getStatus().getError() != null)
                    throw new RuntimeException(queryJob.getStatus().getError().toString());
            }
        } catch (InterruptedException e) {
            ScorecardHandler.LOGGER.fatal("Error en proceso de borrado.");
            e.printStackTrace();
        }
        ScorecardHandler.LOGGER.info("Filas correspondientes eliminadas.");
        return true;
    }

    public boolean isDaily() {
        return daily;
    }

    public void setDaily(boolean daily) {
        this.daily = daily;
    }
}
