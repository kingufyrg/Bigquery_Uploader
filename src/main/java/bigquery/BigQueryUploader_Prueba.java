package bigquery;

import com.betstone.etl.ScorecardHandler;
import com.betstone.etl.enums.BigQueryTable;
import com.betstone.etl.enums.ReportType;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.*;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

// TODO: Cancel Button de Subida
// TODO: Funciones List de Uploader para Interfaz
// TODO: Logger a TextArea


public class BigQueryUploader {

    private BigQuery bigquery;
    private Properties properties;
    private final String propertiesFile = "config.properties";
    private Path credentialPath;
    private GoogleCredentials credentials;
    private String datasetName = "dgc_dna_betstone";
    private String location = "us";
    private BigQueryTable[] tableList = {BigQueryTable.GAME_PROFIT, BigQueryTable.GAME_PROFIT_INVOICING,
            BigQueryTable.PLAYERS_EXPERIENCE, BigQueryTable.ASSETS_DAILY, BigQueryTable.ASSETS_INVOICING,
            BigQueryTable.EGM, BigQueryTable.EGM_INVOICING, BigQueryTable.MISTERY};

    public BigQueryUploader() {
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

    public void writeToFileList(BigQueryTable table) throws IOException {
        Files.list(Paths.get(getFilePath(table)))
                .filter(p -> p.toFile().isFile())
                .forEach(p -> {
                    try {
                        ScorecardHandler.LOGGER.info("Archivo: " + p.getFileName() + " a tabla: "
                                + table.getName());
                        writeToFile(datasetName, table, p.toAbsolutePath().toString());
                        ScorecardHandler.LOGGER.info("Subida completada");
                        Files.delete(p);
                        ScorecardHandler.LOGGER.info("Archivo borrado.");
                    } catch (InterruptedException e) {
                        ScorecardHandler.LOGGER.fatal("Error de interrupción en: " +
                                e.getMessage());
                    } catch (IOException e) {
                        ScorecardHandler.LOGGER.fatal("Error en operacion I/O en: " + e.getMessage());
                    }
                });
    }

    private String getFilePath(BigQueryTable table) throws IOException {
        switch (table) {
            case GAME_PROFIT:
                return getPropertiesValue("upload.directory.gameprofit");
            case GAME_PROFIT_INVOICING:
                return getPropertiesValue("upload.directory.gameprofitinvoicing");
            case EGM:
                return getPropertiesValue("upload.directory.egm");
            case EGM_INVOICING:
                return getPropertiesValue("upload.directory.egminvoicing");
            case MISTERY:
                return getPropertiesValue("upload.directory.mistery");
            case ASSETS_DAILY:
                return getPropertiesValue("upload.directory.assetsdaily");
            case ASSETS_INVOICING:
                return getPropertiesValue("upload.directory.assetsdailyinvoicing");
            case PLAYERS_EXPERIENCE:
                return getPropertiesValue("upload.directory.playersexperience");
        }
        return null;
    }

    public long writeToFile(String datasetName, BigQueryTable tableName, String csvPath)
            throws InterruptedException {
        Field[] fields = ((Gson) toJson()).fromJson(tableName.getSchema(), Field[].class);
        Schema schema = Schema.of(fields);
        TableId tableId = TableId.of(datasetName, tableName.getName());
        WriteChannelConfiguration writeChannelConfiguration =
                WriteChannelConfiguration
                        .newBuilder(tableId)
                        .setFormatOptions(FormatOptions.csv())
                        .setWriteDisposition(JobInfo.WriteDisposition.WRITE_APPEND)
                        .setSchema(schema)
                        .build();

// The location must be specified; other fields can be auto-detected.
        JobId jobId = JobId.newBuilder()
                .setLocation(location)
                .build();

        TableDataWriteChannel writer = bigquery
                .writer(jobId, writeChannelConfiguration);

        // Write data to writer
        try (OutputStream stream = Channels.newOutputStream(writer)) {
            Files.copy(Paths.get(csvPath), stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

// Get load job
        Job job = writer.getJob();
        job = job.waitFor();
        JobStatistics.LoadStatistics stats = job.getStatistics();
        return stats.getOutputRows();
    }

    private Object toJson() {
        JsonDeserializer<LegacySQLTypeName> typeDeserializer = (jsonElement, type, deserializationContext) ->
                LegacySQLTypeName.valueOf(jsonElement.getAsString());
        JsonDeserializer<FieldList> subFieldsDeserializer = (jsonElement, type, deserializationContext) -> {
            Field[] fields = deserializationContext.deserialize(jsonElement.getAsJsonArray(), Field[].class);
            return FieldList.of(fields);
        };

        return new GsonBuilder()
                .registerTypeAdapter(LegacySQLTypeName.class, typeDeserializer)
                .registerTypeAdapter(FieldList.class, subFieldsDeserializer)
                .create();
    }

    public String getPropertiesValue(String property) throws IOException {
        properties = new Properties();
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(propertiesFile);
        return openPropertiesFileConnection(property, inputStream, properties, propertiesFile);
    }

    static String openPropertiesFileConnection(String property, InputStream inputStream, Properties properties, String propertiesFile) throws IOException {
        if (inputStream != null) {
            properties.load(inputStream);
            String date = properties.getProperty(property);
            inputStream.close();
            return date;
        } else
            throw new FileNotFoundException("Property file: " + propertiesFile + " not found.");
    }

    private static boolean isFolderActive(Path path) {
        try {
            return Files.list(path)
                    .anyMatch(p -> p.toFile().isFile());
        } catch (IOException e) {
            ScorecardHandler.LOGGER.fatal("Error al abrir directorio");
        }
        return false;
    }


    public boolean uploadBigQueryFiles() {
        ScorecardHandler.LOGGER.info("\t-------- \tProceso Completo de Subida \t--------");
        try {
            Stream.of(tableList).forEach(bgT -> {
                try {
                    ScorecardHandler.LOGGER.info("Iniciando con tabla: " + bgT.getName());
                    Path p = Paths.get(getFilePath(bgT));
                    ScorecardHandler.LOGGER.info("Path: " + p.toString());
                    while (isFolderActive(p)) {
                        try {
                            ScorecardHandler.LOGGER.info("Folder activo");
                            writeToFileList(bgT);
                        } catch (IOException e) {
                            ScorecardHandler.LOGGER.fatal("Error en subida de un archivo");
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    ScorecardHandler.LOGGER.fatal("Error al leer propiedad.");
                }
            });
        } catch (Exception e) {
            ScorecardHandler.LOGGER.fatal("Error al ejecutar subida de archivos a BigQuery");
        }
        ScorecardHandler.LOGGER.info("Proceso de subida finalizado");
        return false;
    }

    public void uploadBigQueryFilesFromOneDay(ReportType report) {
        ScorecardHandler.LOGGER.info("\t-------- \tProceso Completo de Subida \t--------");
        List<BigQueryTable> tables = getTablesFromReportType(report);

        tables.stream().forEach(table -> {
            ScorecardHandler.LOGGER.info("Iniciando con tabla: " + table.getName());
            Path p = null;
            try {
                p = Paths.get(getFilePath(table));
                while (isFolderActive(p)) {
                    ScorecardHandler.LOGGER.info("Folder activo");
                    writeToFileList(table);
                }
            } catch (Exception e) {
                ScorecardHandler.LOGGER.fatal("Error al ejecutar subida de archivos a BigQuery");
            }
        });
        ScorecardHandler.LOGGER.info("Proceso de subida finalizado");
    }

    private List<BigQueryTable> getTablesFromReportType(ReportType report) {
        switch (report) {
            case ALL_GAME_PROFIT:
                return Lists.newArrayList(BigQueryTable.GAME_PROFIT, BigQueryTable.GAME_PROFIT_INVOICING);
            case SCORECARD_EGM:
                return Lists.newArrayList(BigQueryTable.EGM, BigQueryTable.EGM_INVOICING);
            case MYSTERY:
                return Lists.newArrayList(BigQueryTable.MISTERY);
        }
        return null;
    }
}