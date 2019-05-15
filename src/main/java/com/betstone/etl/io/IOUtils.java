package com.betstone.etl.io;

import com.betstone.etl.ScorecardHandler;
import com.betstone.etl.enums.ReportType;
import com.betstone.etl.enums.SiteType;
import com.betstone.etl.models.Mexico;
import com.betstone.etl.models.Nepal;
import com.betstone.etl.models.Pais;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.stream.Stream;

import static com.betstone.etl.ScorecardHandler.LOGGER;

public class IOUtils {

    private static Properties properties;

    private static String propertiesFile = "config.properties";


    public static final DateTimeFormatter scorecardFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    public static final DateTimeFormatter fileFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static void deleteAllRepeatedExcelFiles(ScorecardHandler scorecard) { // todo: Hay que refactorizar este método

        try {
            Stream<Path> gameProfitDaily =
                    Files.list(Paths.get(getPropertiesValue("output.directory.gameprofit")));
            gameProfitDaily.forEach(path -> {
                try {
                    if (!path.toFile().isDirectory()) {
                        Files.deleteIfExists(Paths.get(
                                getPropertiesValue("output.directory.gameprofit.procesados"),
                                path.getFileName().toString()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            Stream<Path> gameProfitInvoicing =
                    Files.list(Paths.get(getPropertiesValue("output.directory.invoicing.gameprofit")));
            gameProfitInvoicing.forEach(path -> {
                try {
                    if (!path.toFile().isDirectory())
                        Files.deleteIfExists(Paths.get(
                                getPropertiesValue("output.directory.invoicing.gameprofit.procesados"),
                                path.getFileName().toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            Stream<Path> egmDaily =
                    Files.list(Paths.get(getPropertiesValue("output.directory.egm")));
            egmDaily.forEach(path -> {
                try {
                    if (!path.toFile().isDirectory())
                        Files.deleteIfExists(Paths.get(
                                getPropertiesValue("output.directory.egm.procesados"),
                                path.getFileName().toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            Stream<Path> egmInvoicing =
                    Files.list(Paths.get(getPropertiesValue("output.directory.invoicing.egm")));
            egmInvoicing.forEach(path -> {
                try {
                    if (!path.toFile().isDirectory())
                        Files.deleteIfExists(Paths.get(
                                getPropertiesValue("output.directory.invoicing.egm.procesados"),
                                path.getFileName().toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            Stream<Path> mistery =
                    Files.list(Paths.get(getPropertiesValue("output.directory.mistery")));
            mistery.forEach(path -> {
                try {
                    if (!path.toFile().isDirectory())
                        Files.deleteIfExists(Paths.get(
                                getPropertiesValue("output.directory.mistery.procesados"),
                                path.getFileName().toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void filterFormatFilesAndMove(Path downloadPath, Path directoryPath, String endsWith) throws IOException {
        Files.list(downloadPath).filter(p ->
        {
            try {
                return p.getFileName().toString().endsWith(endsWith) &&
                        getCreateTimeDate(p).getDayOfYear() == LocalDate.now().getDayOfYear();
            } catch (IOException e) {
                LOGGER.fatal(e.getMessage());
            }
            return false;
        }).findFirst()
                .ifPresent(path -> {
                    try {
                        LOGGER.info("Archivo: " + path);
                        Files.move(path, directoryPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        LOGGER.fatal(e.getMessage());
                    }
                });
    }

    /**
     * Obtiene metadato 'Fecha de Creacion' de un archivo localizado en el path ingresado y lo convierte a LocalDate.
     *
     * @param p Path del archivo a explorar.
     * @return Objeto LocalDate con la fecha de creación del archivo.
     * @throws IOException Si el archivo no existe
     */
    private static LocalDate getCreateTimeDate(Path p) throws IOException {
        FileTime fileTime = Files.readAttributes(p,
                BasicFileAttributes.class).creationTime();
        return LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault()).toLocalDate();
    }


    /**
     * Borra todos los archivos tipo .crdownload de la carpeta asociada en la propiedad "directoty.download"
     */
    public static void eraseAllIncompleteDownloads() {
        LOGGER.info("Comenzando búsqueda y borrado de descargas incompletas.");
        try {
            Path downloadPath = Paths.get(getPropertiesValue("directory.download"));
            Files.list(downloadPath)
                    .filter(p -> p.toString().endsWith(".crdownload"))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            LOGGER.fatal("Error al eliminar archivo: " + path.getFileName());
                            LOGGER.fatal(e.getMessage());
                        }
                    });
            LOGGER.info("Descargas incompletas borradas.");
        } catch (IOException e) {
            LOGGER.fatal(e.getMessage());
        }
    }

    public static void eraseAllFormatFiles(String endsWith) {
        LOGGER.info("Comenzando búsqueda y borrado de archivos tipo: " + endsWith);
        try {
            Path downloadPath = Paths.get(getPropertiesValue("directory.download"));
            Files.list(downloadPath)
                    .filter(p -> p.toString().endsWith(endsWith))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            LOGGER.fatal("Error al eliminar el archivo: " + e.getMessage());
                        }
                    });
            LOGGER.info("Archivos io borrados.");
        } catch (IOException e) {
            LOGGER.fatal(e.getMessage());
        }
    }


    /**
     * Busca el archivo descargado tratando de empatar que sea un archivo que termine en .xls
     * y que su fecha de creación sea el de hoy. Una vez encontrado, envía el archivo
     * encontrado a la dirección de descarga.
     *
     * @param pais País que contiene la fecha de ejecución del proceso.
     */
    public static void findFileAndMove(ReportType reportType, Pais pais, boolean daily, boolean formatExcel) {
        if (reportType == ReportType.MYSTERY && !(pais instanceof Mexico))
            return;
        try {
            String pathS = formatOutputName(reportType, pais, formatExcel);
            Path downloadPath = Paths.get(getPropertiesValue("directory.download")),
                    directoryPath = getDirectoryPathFromReport(reportType, pathS, daily);
            String endsWith = (formatExcel) ? ".xls" : ".csv";
            IOUtils.filterFormatFilesAndMove(downloadPath, directoryPath, endsWith);
            LOGGER.info("Archivo descargado movido a: " + directoryPath);
        } catch (IOException e) {
            LOGGER.fatal(e.getMessage());
        }
    }

    private static String formatOutputName(ReportType reportType, Pais pais, boolean formatExcel) {
        String name = getDateFormatted(pais.getFecha(), fileFormat);
        switch (pais.getCountryType()) {
            case MEXICO:
                name += 1;
                break;
            case LAOS:
                name += 2;
                break;
            case NEPAL:
                if (((Nepal) pais).getSiteType() == SiteType.SHANGRI)
                    name += 3;
                else name += 4;
                break;
            case THURKS:
                name += 5;
        }

        switch (reportType) {
            case ALL_GAME_PROFIT:
                name += 1;
                break;
            case SCORECARD_EGM:
                name += 2;
                break;
            case MYSTERY:
                name += 3;
        }
        return name + ((formatExcel) ?
                ".xls" :
                ".csv");
    }

    /**
     * Obtiene una fecha formateada en tipo MES/DIA/AÑO, desde un objeto LocalDate.
     *
     * @param date Fecha a formatear
     * @return String con fecha formateada.
     */
    public static String getDateFormatted(LocalDate date, DateTimeFormatter format) {
        return date.format(format);
    }


    /**
     * Hace una lectura rápida del archivo .properties y obtiene el valor de una propiedad definida
     * dentro del archivo.
     *
     * @param property propiedad a buscar
     * @return String con el valor de la propiedad
     * @throws IOException Si no se encuentra el archivo .properties
     */
    public static String getPropertiesValue(String property) throws IOException {
        properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(propertiesFile);
        } catch (FileNotFoundException e) {
            inputStream = ClassLoader.getSystemResourceAsStream(propertiesFile);
        } finally {
            properties.load(inputStream);
            String date = properties.getProperty(property);
            inputStream.close();
            return date;
        }
    }

    /**
     * Define un valor de una propiedad en el archivo .properties.
     *
     * @param property Propiedad a buscar
     * @param value    Valor nuevo de la propiedad
     * @throws IOException Si no encuentre el archivo .properties
     */
    public static void setPropertiesValue(String property, String value) throws IOException {
        properties = new Properties();
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(propertiesFile);
        if (inputStream != null) {
            properties.load(inputStream);
            properties.setProperty(property, value);
            inputStream.close();
        } else {
            inputStream = new FileInputStream(propertiesFile);
            properties.load(inputStream);
            properties.setProperty(property, value);
            inputStream.close();
        }
    }

    public static Path getDirectoryPathFromReport(ReportType reportType, String pathS, boolean daily) throws IOException {
        switch (reportType) {
            case ALL_GAME_PROFIT:
                if (daily)
                    return Paths.get(getPropertiesValue("output.directory.gameprofit"),
                            pathS);
                else
                    return Paths.get(getPropertiesValue("output.directory.invoicing.gameprofit"),
                            pathS);
            case SCORECARD_EGM:
                if (daily)
                    return Paths.get(getPropertiesValue("output.directory.egm"),
                            pathS);
                else
                    return Paths.get(getPropertiesValue("output.directory.invoicing.egm"),
                            pathS);
            case MYSTERY:
                return Paths.get(getPropertiesValue("output.directory.mistery"),
                        pathS);
        }
        return null;
    }

    public static void setPropertiesFile(String propertiesFile) {
        IOUtils.propertiesFile = propertiesFile;
    }


    public static void findFileAndMoveSegmented(ReportType reportType, Pais pais, boolean daily, int segment, String endsWith) throws IOException {
        if (reportType == ReportType.MYSTERY && !(pais instanceof Mexico))
            return;
        try {
            String pathS = formatOutputNameSegmented(reportType, pais, segment);
            Path downloadPath = Paths.get(getPropertiesValue("directory.download")),
                    directoryPath = getDirectoryPathFromReport(reportType, pathS, daily);
            IOUtils.filterFormatFilesAndMove(downloadPath, directoryPath, endsWith);
            LOGGER.info("Archivo descargado y en: " + directoryPath);
        } catch (IOException e) {
            LOGGER.fatal(e.getMessage());
        }
    }

    private static String formatOutputNameSegmented(ReportType reportType, Pais pais, int segmentCasinos) {
        String name = IOUtils.getDateFormatted(pais.getFecha(), fileFormat);
        return name + "11" + "_" + segmentCasinos + ".xls";
    }

    public static LocalDate getLastSunday() {
        LocalDate today = LocalDate.now();
        if (today.getDayOfWeek() == DayOfWeek.SUNDAY)
            today.minusDays(1);
        while (today.getDayOfWeek() != DayOfWeek.SUNDAY) {
            today = today.minusDays(1);
        }
        return today;
    }
}
