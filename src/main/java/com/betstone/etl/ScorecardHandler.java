package com.betstone.etl;

import com.betstone.etl.enums.*;
import com.betstone.etl.io.IOUtils;
import com.betstone.etl.models.*;
import com.betstone.etl.predicates.CasinoOperatorsPredicates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.betstone.etl.io.IOUtils.*;

public class ScorecardHandler {

    private static final long TIME_POLL = 10;
    public static Logger LOGGER = LogManager.getLogger(ScorecardHandler.class);
    private final String egmReportWE = "//*[@id=\"tab-content-1\"]/div/reports-tab/md-content/div[3]/div/div/div[2]/a[2]";
    private final String gPReportWE = "#dtReportLayouts #colLayout_Row4";
    private String misteryReportWE = "#dtReportLayouts #colLayout_Row6";
    private String quickEGM_GPWE = "#ReportList_BillinReports #colLayout_Row1";

    private boolean formatExcel = true;


    public static String totalInCellId = "#colTotalWagers span";
    public static String ggrCellId = "#colBonusWinAmount";
    public static String totalOutCellId = "#colTotalPayouts span";
    public static String ggrProfit = "#colGGR span";
    private final String inputFromDateId = "inpFromDate";
    private final String inputToDateId = "inpToDate";
    private final String inputCurrencyId = "inpCurrency";
    private final String inputOperatorId = "inpOperator_handler";
    private final String operatorsListId = "#inpOperator li";
    private final String inputCountry_handlerId = "inpCountry_handler";
    private final String inpCountryListId = "#inpCountry li";
    private final String inpCasinoOperator_handlerId = "inpCasinoOperator_handler";
    private final String inpCasinoOperatorListId = "#inpCasinoOperator li";
    private final String refreshButtonId = "rdAgDefaultButtonStyle";
    private final String imgTableExportId = "/html/body/div[1]/div/div[2]/md-content/md-tabs/md-tabs-content-wrapper/md-tab-content[1]/div/table-tab/div[1]/div[1]/div/div/div/md-menu[1]/button";
    private final String exportExcelButtonId = "//*[@ng-click=\"exportFile('excel')\"]";
    private final String exportCsvButtonId = "lblExportCsv_rdPopupOptionItem";
    private final String inpSite_handlerId = "inpSite_handler";
    private final String inpSiteListId = "#inpSite li";

    private final String baseURL = "https://biportal.betstone.com/User/Login?ReturnUrl=%2f";

    static WebDriver driver;
    private WebElement dateFromSubmit;
    private WebElement dateToSubmit;
    private WebElement currencyWE;
    private WebElement operatorWE;
    private WebElement countryWE;
    private WebDriverWait wait;
    private WebElement casinoOperatorWE;
    private WebElement refreshWE;
    private WebElement exportIconWE;
    private WebElement downloadWE;
    private WebElement sitesWE;

    ArrayList<String> tabs;

    private LocalDate initDay;
    private LocalDate finalDay;
    private int segmentCasinos = 1;
    private boolean daily = true;

    private int ERROR_COUNT = 0;
    private String username;
    private String password;
    public String amount = null;

    /**
     * Ejecuta el método oneDayDownloadCountry para una lista de Paises, con la lista de Reportes especificado, de un sólo día.
     *
     * @param paisList    Lista de Objetos País.
     * @param reportTypes Lista de enums ReportType
     */
    public void oneDayDownloadCountryList(List<Pais> paisList, List<ReportType> reportTypes) {
        Stream<Pais> paisStream = paisList.stream();
        try {
            paisStream.forEach(pais -> reportTypes.stream()
                    .forEach(reportType -> oneDayDownloadCountry(reportType, pais)));
        } catch (Exception e) {
            LOGGER.fatal(e.getMessage());
        } finally {
            cancelProcess();
        }
    }


    /**
     * Ejecuta el método oneWeekDownloadCountry para una lista de países, de una lista específica de reportes, para una semana.
     *
     * @param paisList    Lista de objetos País
     * @param reportTypes Lista de enums ReportType
     */
    public void oneWeekDownloadCountryList(List<Pais> paisList, List<ReportType> reportTypes) {
        Stream<Pais> paisStream = paisList.stream();
        paisStream.forEach(pais -> reportTypes.stream()
                .forEach(reportType ->
                        oneWeekDownloadCountry(reportType, pais)));
        cancelProcess();
    }

    /**
     * Ejecuta el método oneMonthDownloadCountry para una lista de países, de una lista específica de reportes, de un mes.
     *
     * @param paisList    Lista de objetos País
     * @param reportTypes Lista de enums ReportType
     */
    public void oneMonthDownloadCountryList(List<Pais> paisList, List<ReportType> reportTypes) {
        Stream<Pais> paisStream = paisList.stream();
        Stream<ReportType> reportTypeStream = reportTypes.stream();
        paisStream.forEach(pais -> reportTypeStream.forEach(reportType ->
                oneMonthDownloadCountry(reportType, pais)
        ));
    }

    /**
     * Método atómico de descarga de Scorecard que sirve como método principal para nuevas tareas de descarga.
     * <p>
     * Ejecuta la extracción de un archivo de datos correspondiente a un solo día, de un sólo reporte que contenga las
     * especificaciones dadas por los parámetros reportType y t.
     * <p>
     * 1. Se inicializa el driver de chrome, buscando desde el directorio especificado en el archivo properties su ubicación.
     * 2. Se abre el navegador y se ingresa a la plataforma de Scorecard desde el usuario y la contraseña especificado en el archivo de properties.
     * 3. Si el país es México, con el tipo de reporte GAME_PROFIT, con el uso del formato excel activado, se hace una descarga segmentada de archivos.
     * Si no, se hace una descarga completa de datos.
     * <p>
     * Manejo de Errores:
     * Existe la posibilidad de que existan los siguientes tipos de erroes,
     * 1. Error de lectura de archivo (IOException): Se detiene la ejecución completa y se loggea el error
     * 2. Error por tiempo transcurrido (TimeoutException): Se loggea el error, se verifica si existe un tipo de
     * error como código HTML para poder resolverse recargando o reiniciar el proceso.
     * 3. Cualquier otra excepcion (Exception): Se cancela el proceso y se reinicia.
     *
     * @param reportType Tipo de enum ReportType. Especifica el reporte a descargar.
     * @param t          Objeto heredado de País. Especifica el país a descargar.
     * @param <T>        Parámetro genérico para especificar la entrada solo de objetos heredados de País
     * @return Verdadero si el proceso se ejecuto sin problemas, de lo contrario falso.
     */
    public <T extends Pais> boolean oneDayDownloadCountry(ReportType reportType, T t) {
        LOGGER.info("\t-------- \tPaís: " + t.getCountryType().getName() + "\t--------");
        LOGGER.info("\t-------- \tDía: " + IOUtils.getDateFormatted(t.getFecha(), scorecardFormat) + "\t--------");
        LOGGER.info("\t-------- \tReporte: " + reportType.name() + "\t--------");

            try {
                initializeWebBrowser();
                    scorecardInputProcessAndDownloadExcel1(reportType,t);
                if(!amount.isEmpty()){
                    scorecardInputProcessAndDownloadExcel(reportType, t);

                    findFileAndMove(reportType, t, isDaily(), formatExcel);}
            } catch (NullPointerException e){
                LOGGER.info("Finalizado por Reporte Vacío");
            }
            catch (IOException e) {
                LOGGER.fatal(e.getMessage());
            } catch (TimeoutException e) {
             LOGGER.warn("Error en proceso, reintentando");
             verifyHTTPCode();
             oneDayDownloadCountry(reportType, t);
             }
            catch (Exception e) {
                LOGGER.fatal("Error inesperado: " + e.getMessage() +
                        " en:\n " + Stream.of(e.getStackTrace()).toString());
                LOGGER.info("Reintendando...");
                ERROR_COUNT++;
                cancelProcess();
                try {
                    if (ERROR_COUNT >= 5) {
                        Thread.sleep(5000 * 60);
                        ERROR_COUNT = 0;
                    }
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                oneDayDownloadCountry(reportType, t);
            }

        return true;
    }


    /**
     * Ejecuta la extracción de los archivos correspondientes a una semana de un país
     * específico, de un tipo de reporte. Hace la ejecución iterada del método oneDayDownloadCountry 7 veces.
     *
     * @param reportType Tipo de reporte a extraer
     * @param t          País al cual se extraerá.
     * @param <T>        Parámetro genérico para objetos heredados de País.
     * @return Verdadero si el proceso se ejecutó sin problemas, de lo contrario falso.
     */
    public <T extends Pais> boolean oneWeekDownloadCountry(ReportType reportType, T t) {
        finalDay = t.getFecha();
        initDay = setWeekBeforeDates(finalDay);
        LocalDate currentDay = initDay;
        t.setFecha(initDay);
        /**  while (!currentDay.isEqual(finalDay.plusDays(1))) {*/
        while (!currentDay.isEqual(finalDay)) {
            oneDayDownloadCountry(reportType, t);
            currentDay = currentDay.plusDays(1);
            t.setFecha(currentDay);
        }
        t.setFecha(finalDay);
        LOGGER.info("Proceso de extracción completado para: \n"
                + "Reporte: " + reportType.name() + "\nPais: " + t.getCountryType().getName());
        return true;
    }

    /**
     * Ejecuta la extracción de los archivos correspondientes de un mes, de un país
     * específico, de un tipo de reporte. Hace la ejecución iterada del método oneDayDownloadCountry
     * 30 veces.
     *
     * @param reportType Tipo de reporte a extraer
     * @param t          País al cual se extraerá.
     * @param <T>        Parámetro genérico para objetos heredados de País.
     * @return Verdadero si el proceso se ejecutó sin problemas, de lo contrario falso.
     */
    public <T extends Pais> boolean oneMonthDownloadCountry(ReportType reportType, T t) {
        finalDay = t.getFecha();
        initDay = finalDay.minusDays(30);
        LocalDate currentDay = initDay;
        t.setFecha(initDay);
        while (!currentDay.isEqual(finalDay.plusDays(1))) {
            oneDayDownloadCountry(reportType, t);
            currentDay = currentDay.plusDays(1);
            t.setFecha(currentDay);
        }
        LOGGER.info("Proceso de extracción completado para: \n"
                + "Reporte: " + reportType.name() + "\nPais: " + t.getCountryType().getName());
        return true;
    }

    /**
     * Se encarga de la ejecución de los métodos scorecardInputProcessAndDownloadExcelSegmented y
     * findFileAndMoveSegmented X veces para un reporte con distintas segmentaciones.
     *
     * @param reportType Enum ReportType del reporte a descargar.
     * @param t          Objeto heredado de Pais a descargar
     * @param <T>        Genérico País
     * @throws Exception
     */
    private <T extends Pais> void segmentMexicoReports(ReportType reportType, T t) throws Exception {
        int totalSegments = CasinoOperators.getSegmentSizeFromCountry(t.getCountryType());
        scorecardInputProcessAndDownloadExcel1(reportType,t);
        for (segmentCasinos = 1; segmentCasinos <= totalSegments; segmentCasinos++) {

            if(!amount.isEmpty()){
            scorecardInputProcessAndDownloadExcelSegmented(reportType, t);
            findFileAndMoveSegmented(reportType, t, isDaily(), segmentCasinos, ".xlsx");}
        }
    }

    /**
     * Ejecuta una manipulación en Scorecard para obtener los datos de los reportes en Scorecard para un día de un país, de un reporte. Se puede
     * especificar en una lista o en un conjunto de Strings, los Ids de los elementos web que contienen los datos
     * numéricos a obtener.
     *
     * @param reportType Enum ReportType del reporte.
     * @param t          Objeto heredado de Pais.
     * @param elementIds Conjunto de Strings que corresponden a los Ids en CSS Selector de los datos a retornar.
     * @param <T>        Genérico para Heredados de Páis.
     * @return Lista de Double's de la información de los elementIds.
     */
    public <T extends Pais> List<Double> profitFromOneDayOneCountry(ReportType reportType, T t, String... elementIds) {
        try {
            initializeWebBrowser();
            scorecardInputProcess(reportType, t);
            List<Double> values = new ArrayList<>();
            CargaReporte(reportType);
            verifyReportLoadedCorrectly(reportType);
            if(!amount.isEmpty()){
            Arrays.stream(elementIds).forEach(s -> {
                try {
                    values.add(returnValuesFromReport(reportType, s));
                } catch (TimeoutException e) {
                    ScorecardHandler.LOGGER.warn("Error en extracción de valor. Posible falla de carga. Reintentando");
                    profitFromOneDayOneCountry(reportType, t, elementIds);
                }
            });
            return values;}
        } catch (StaleElementReferenceException e) {
            LOGGER.fatal("Error al obtener datos, reintentando...");
            LOGGER.fatal(e.getMessage());
            driver.navigate().refresh();

            profitFromOneDayOneCountry(reportType, t, elementIds);
        } catch (Exception e) {
            LOGGER.fatal("Error al obtener datos, reintentando...");
            LOGGER.fatal(e.getMessage());
            cancelProcess();
            try {
                Thread.sleep(1000 * 60 * 1);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            profitFromOneDayOneCountry(reportType, t, elementIds);
        }
        return null;
    }

    /**
     * Obtiene el código HTTP de la página enfocada del driver de chrome. Si contiene un código de error, recarga la
     * página.
     */
    private void verifyHTTPCode() {
        try {
            HttpURLConnection url = (HttpURLConnection) new URL(driver.getCurrentUrl()).openConnection();
            int code = url.getResponseCode();
            if (code >= 400)
                driver.navigate().refresh();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }


    /**
     * Encuentra los elementos web especificados por el parámetro elementId, copia el texto, lo transforma a numérico
     * y lo devuelve. Verifica también si el reporte se cargó correctamente al buscar un texto en el reporte que
     * siempre estaría presente, de haber cargado bien.
     *
     * @param reportType Enum ReportType del reporte a descargar.
     * @param elementId  Id en CSS Selector, del elemento web de texto que contiene la cantidad a devolver.
     * @return Double del texto contenido en el elemento web.
     * @throws TimeoutException
     */
    private Double returnValuesFromReport(ReportType reportType, String elementId) throws TimeoutException {
        tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1));
        WebElement element = wait.until(a -> driver.findElement(
                By.cssSelector(elementId)
        ));
        /**if (!verifyReportLoadedCorrectly(reportType))
            throw new TimeoutException();*/
        return castToDouble(element);
    }

    /**
     * Extrae el texto del elemento web, lo formatea, y convierte a tipo Double.
     *
     * @param element
     * @return
     */
    private Double castToDouble(WebElement element) {
        String formatted = element.getText();
        if (formatted.substring(formatted.length() - 3).contains(","))
            formatted = formatted.replaceAll("[.]", "")
                    .replace(",", ".");
        else
            formatted = formatted.replaceAll("[,]", "");
        return (Double.valueOf(formatted));
    }


    /**
     * Obtiene información sobre las pestañas abiertas por el driver, mantiene el focus en la segunda,
     * la cierra y devuelve el focus a la primera pestaña.
     */
    private void returnToMainMenu() {
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        if (tabs.size() > 1) {
            driver.switchTo().window(tabs.get(1));
            driver.close();
        }
        driver.switchTo().window(tabs.get(0));
    }

    /**
     * Inicializa el explorador de Chrome. Si el explorador ya está inicializado, retorna.
     *
     * @return Verdadero si se ejecutó sin problemas.
     */
    private boolean initializeWebBrowser() throws IOException {
        if (driver != null && wait != null)
            return true;
        else if (driver != null && wait == null) {
            setWaitDriver(TIME_POLL);
            return true;
        }
        setWebDriverProperty();
        ChromeOptions options = new ChromeOptions();
        /**options.addArguments("-incognito");*/
        driver = instanceDriver(options);

        driver.manage().window().maximize();
        LOGGER.info("WebDriver inicializado.");
        setWaitDriver(TIME_POLL);
        return true;
    }

    /**
     * Define la propiedad para el sistema Java de donde tomará el driver de Chrome. La ruta del driver se obtiene
     * desde el archivo properties.
     */
    private void setWebDriverProperty() throws IOException {
        System.setProperty("webdriver.chrome.driver", getPropertiesValue("chrome.driver"));
        LOGGER.info("Chromer Driver agregado.");
    }

    /**
     * Obtiene una nueva instancia de ChromeDriver con las opciones ingresadas por parámetro.
     *
     * @param options Opciones a agregar al nuevo driver de chrome.
     * @return Nueva instancia de ChromeDriver.
     */
    private WebDriver instanceDriver(ChromeOptions options) {
        return new ChromeDriver(options);
    }

    /**
     * Define las fechas en memoria para trabajar una semana antes de la fecha ingresada.
     *
     * @param date Fecha de referencia.
     */
    private LocalDate setWeekBeforeDates(LocalDate date) {
        LOGGER.info("Fecha de inicio de la semana: " + IOUtils.getDateFormatted(date, scorecardFormat));
        return date.minusDays(6);
    }


    /**
     * Proceso de ingreso de variables y botones en BI Portal.
     *
     * @param reportType Reporte a procesar
     * @param pais       País a ingresar.
     */
    private void scorecardInputProcessAndDownloadExcel(ReportType reportType, Pais pais) throws Exception {
        returnToMainMenuOrLoadPage();
        if (reportType == ReportType.MYSTERY && (!(pais instanceof Mexico))) {
            returnToMainMenu();
            return;
        }
        eraseAllIncompleteDownloads();
        IOUtils.eraseAllFormatFiles((isFormatExcel()) ? ".xlsx" : ".csv");
        openReport(pais, reportType);
        Wait_for_report(reportType);
        inputDates(pais,reportType);
        setWebCurrency(pais);
        setCountry(pais);
        if (pais instanceof Nepal) {
            setWaitDriver(TIME_POLL);
            setSite(pais);
        }
        refreshButton();
        LOGGER.info("Prueba de descarga");
        verifyReportLoadedCorrectly(reportType);
        downloadExcel(reportType);
        waitDownloadComplete(pais, reportType, false);
    }

    private void scorecardInputProcessAndDownloadExcel1(ReportType reportType, Pais pais) throws Exception {
        returnToMainMenuOrLoadPage();
        LOGGER.info("Entrando a BI Portal");
        if (reportType == ReportType.MYSTERY && !(pais instanceof Mexico)) {
            returnToMainMenu();
            return;
        }
        eraseAllIncompleteDownloads();
        IOUtils.eraseAllFormatFiles((isFormatExcel()) ? ".xlsx" : ".csv");
        openReport(pais, reportType);
        Wait_for_report(reportType);
        inputDates(pais,reportType);
        setWebCurrency(pais);
        setCountry(pais);
        if (pais instanceof Nepal) {
            setWaitDriver(TIME_POLL);
            setSite(pais);
        }
        refreshButton();
        verifyReportLoadedCorrectly(reportType);
    }

    /**
     * @param reportType
     * @param pais
     * @throws Exception
     */
    private void scorecardInputProcessAndDownloadExcelSegmented(ReportType reportType, Pais pais) throws Exception {
        returnToMainMenuOrLoadPage();
        if (reportType == ReportType.MYSTERY && !(pais instanceof Mexico)) {
            returnToMainMenu();
            return;
        }
        eraseAllIncompleteDownloads();
        IOUtils.eraseAllFormatFiles((isFormatExcel()) ? ".xlsx" : ".csv");
    }
        /**inputDates(pais);
        setWebCurrency(pais);
        setWebDistributor();
        setCountry(pais);
        setCasinoOperatorSegmented(pais);
        if (pais instanceof Nepal)
            setSite(pais);
        refreshButton();
        openReport(reportType);

        downloadExcel(reportType);
        waitDownloadComplete(pais, reportType, false);
        setNewLastDateProperty(pais);*/


    /**
     * Proceso de ingreso de variables y botones en ScoreCard.
     *
     * @param reportType Reporte a procesar
     * @param pais       País a ingresar.
     */
    private void scorecardInputProcess(ReportType reportType, Pais pais) {
        if (driver.getWindowHandles().size() == 1) {
            try {
                login(driver);
            } catch (IOException e) {
                LOGGER.fatal(e.getMessage());
            }
        } else
            returnToMainMenu();

        inputDates(pais,reportType);
        setWebCurrency(pais);
        setWebDistributor();
        setCountry(pais);
        setCasinoOperator(pais);
        if (pais instanceof Nepal)
            setSite(pais);
        refreshButton();
        openQuickReport(reportType);
    }

    /**
     * Vuelve a la pestaña principal del navegador Chrome. Verifica si ya se ingresó a Scorecard, sino hace Login.
     *
     * @throws IOException
     */
    private void returnToMainMenuOrLoadPage() throws IOException {
        if (driver.getWindowHandles().size() == 1 && (driver.getCurrentUrl().contains("Logon") ||
                driver.getCurrentUrl().contains("data"))) {
            login(driver);
        } else
            returnToMainMenu();
    }

    private void Template_EGM_Analysis()  {
       try{
           wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"tab-content-1\"]/div/reports-tab/md-content/div[3]/div/div/div[2]/a[2]"))).click();
       }
         catch(Exception e) {
             returnToMainMenu();
         }
    }
    /**
     * Ingresa las opciones de Site para variables en Scorecard, relevante sólo para Nepal. Verifica qué sitio es
     * para poder definir si el tipo de moneda es US o es Rupee.
     *
     * @param pais País a ingresar.
     */
    private void setSite(Pais pais) {
        setWaitDriver(TIME_POLL);
        WebElement c = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#filter_336_MultiSelect")));
        setWaitDriver(TIME_POLL);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();",c);
        setWaitDriver(TIME_POLL);
        Nepal nepal = (Nepal) pais;
        if (nepal.getSiteType() == SiteType.TIGER_PALACE) {
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[6]/md-select-menu/md-content/md-select-header/div[1]/input"))).sendKeys(SiteType.TIGER_PALACE.getSiteName());
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[6]/md-select-menu/md-content/md-option"))).click();

            LOGGER.info("Site Seleccionado: " + SiteType.TIGER_PALACE.getSiteName());
        } else {
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[6]/md-select-menu/md-content/md-select-header/div[1]/input"))).sendKeys(SiteType.SHANGRI.getSiteName());
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[6]/md-select-menu/md-content/md-option"))).click();
            LOGGER.info("Site Seleccionado: " + SiteType.SHANGRI.getSiteName());
        }
        LOGGER.info("Sites definidos.");
    }

    /**
     * Ingresa en archivo .properties la última fecha procesada para fines de persistencia.
     *
     * @param pais Pais que contiene como variable la fecha procesada.
     */
    private void setNewLastDateProperty(Pais pais) {
        try {
            IOUtils.setPropertiesValue("last.date.begin", IOUtils.getDateFormatted(pais.getFecha(), scorecardFormat));
        } catch (IOException e) {
            LOGGER.fatal("Error al leer propiedad: " + e.getMessage());
        }
    }

    /**
     * Revisa el directorio de descargas especificado desde el archivo .properties.
     * Espera a que un archivo .crdownload aparezca en el directorio, una vez positivo,
     * espera a que el directorio no contenga archivos .crdownload. Hace a su vez, pausas entre peticiones
     * dependidendo el reporte y el país para no cargar de una búsqueda pesada de archivos, los cuales
     * podrían reducir drásticamente el rendimiento de la computadora.
     */
    private void waitDownloadComplete(Pais pais, ReportType reportType, boolean operatorDownload) {
        try {
            long sleep = 1000;
            Path downloadPath = Paths.get(getPropertiesValue("directory.download"));
            if (reportType == ReportType.ALL_GAME_PROFIT) {
            while (!Files.list(downloadPath)
                    .anyMatch(p -> p.toString().endsWith(".crdownload"))) {
                if (pais instanceof Mexico && reportType != ReportType.MYSTERY)
                    Thread.sleep(sleep);
            }
            LOGGER.info("Descarga iniciada...");

            while (Files.list(downloadPath)
                    .anyMatch(p -> p.toString().endsWith(".crdownload"))) {
                if (pais instanceof Mexico)
                    Thread.sleep(sleep);
            }
            }
            LOGGER.info("Descarga finalizada...");
            Thread.sleep(3000);
        } catch (IOException | InterruptedException e) {
            LOGGER.fatal(e.getMessage());
        }
    }

    /**
     * Define el objeto WebDriverWait para esperar en cada acción dentro del browser. Automáticamente hace un
     * poll de 500 milisegundos para obtener resultados.
     *
     * @param time Tiempo de espera a esperar.
     */
    private void setWaitDriver(long time) {
        wait = new WebDriverWait(driver, time);
    }

    /**
     * Ingresa URL de login para Scorecard e ingresa tanto usuario como contraseña.
     *
     * @param driver Del browser
     * @return
     */
    private boolean login(WebDriver driver) throws IOException {
        driver.get(baseURL);
        username = getPropertiesValue("rd.username");
        password = getPropertiesValue("rd.password");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"password\"]"))).click();
        driver.findElement(By.xpath("//*[@id=\"username\"]")).sendKeys(username);
        driver.findElement(By.xpath("//*[@id=\"password\"]")).sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"loginButton\"]/span"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"tab-content-1\"]/div/reports-tab/md-content/div[2]/div/div/div[2]/a[2]")));
        LOGGER.info("Inicio de Sesión correcto");
        return true;
    }

    /**
     * Ingresa fecha en el campo Fecha de la Página WEB.
     *
     * @param pais País que contiene la fecha a ingresar.
     */
    private void inputDates(Pais pais, ReportType reportType) {
        LOGGER.info("Ingresando Fecha");
        if(reportType!=ReportType.MYSTERY) {
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"filter_1_Date1\"]/div[1]/input")));
            dateFromSubmit = driver.findElement(By.xpath("//*[@id=\"filter_1_Date1\"]/div[1]/input"));
        } else {
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"filter_341_Date1\"]/div[1]/input")));
            dateFromSubmit = driver.findElement(By.xpath("//*[@id=\"filter_341_Date1\"]/div[1]/input"));
        }
            dateFromSubmit.clear();
            dateFromSubmit.sendKeys(IOUtils.getDateFormatted(pais.getFecha().plusDays(1), scorecardFormat));
            setWaitDriver(TIME_POLL);
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div[1]/div/div[2]/md-sidenav/md-tabs/md-tabs-content-wrapper/md-tab-content[1]/div/form/div[1]/div[3]/div/div/md-datepicker/div/button"))).click();
            setWaitDriver(TIME_POLL);

        try {
            wait.until(ExpectedConditions.attributeToBe(By.xpath("//td[@class=\"md-calendar-date md-calendar-selected-date md-focus\"]"), "aria-label", "Wednesday January 1 2020"));

        }
        catch (Exception exception) {
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//td[@aria-label=\"" + pais.getFecha().getDayOfWeek().getDisplayName(TextStyle.FULL,Locale.ENGLISH) + " " + pais.getFecha().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + pais.getFecha().getDayOfMonth() + " " +  pais.getFecha().getYear() + "\"]"))).click();
        }/*dateToSubmit = driver.findElement(By.id(inputToDateId));
        dateToSubmit.clear();
        dateToSubmit.sendKeys(IOUtils.getDateFormatted(pais.getFecha(), scorecardFormat));
        */
    }

    /**
     * Ingresa tipo de moneda en los elementos web dependiendo el país y el tipo de sitio esperado.
     *
     * @param pais Pais a ingresar
     */
    private void setWebCurrency(Pais pais) {
        currencyWE = driver.findElement(By.xpath("//*[@id=\"filter_340_Select\"]"));


        /**setWaitDriver(TIME_POLL);
        WebElement currencyWE = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#select_value_label_116 > span._md-select-icon")));
        setWaitDriver(TIME_POLL);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();",currencyWE);
        LOGGER.info("Click en Currency");
        setWaitDriver(TIME_POLL);
        WebElement currencyCountry = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#input_117")));*/


            LOGGER.info("Moneda Seleccionada: " + Mexico.CURRENCY);

        switch (pais.getCountryType()) {
            case MEXICO:
                currencyWE.sendKeys(Mexico.CURRENCY);
                setWaitDriver(TIME_POLL);
                break;

            case LAOS:
                currencyWE.sendKeys(Laos.CURRENCY);
                break;

            case NEPAL:
                if (((Nepal) pais).getSiteType() == SiteType.SHANGRI)
                    currencyWE.sendKeys(Nepal.US_CURRENCY);
                else
                    currencyWE.sendKeys(Nepal.INDIAN_CURRENCY);
                break;

            case SPAIN:
                currencyWE.sendKeys(Spain.CURRENCY);
                break;

            case THURKS:
                currencyWE.sendKeys(Thurks.CURRENCY);
                break;

            default:
                LOGGER.fatal("Problema al ingresar tipo de moneda.");
        }
        LOGGER.info("Tipo de moneda definida.");
    }

    /**
     * Encuentra el elemento web 'inpOperator_handler', el cual es el combobox para seleccionar el distribuidor.
     * Posteriormente obtiene todas las opciones posibles. Filtra aquellos elementos que empatan con las caracterízticas
     * del país y el tipo de reporte, y deselecciona (si exixste algún otro seleccionado) aquellos que no.
     */
    private void setWebDistributor() {
        operatorWE = driver.findElement(By.id(inputOperatorId));
        operatorWE.click();
        List<WebElement> operators = driver.findElements(By.cssSelector(operatorsListId));
        try {
            operators.stream()
                    .filter(opt -> opt.isDisplayed() && !opt.findElement(By.cssSelector("input")).isSelected()
                            && opt.getText().equalsIgnoreCase(Operator.BETSTONE.toString()))
                    .forEach(option -> option.click());

            operators.stream()
                    .filter(opt -> opt.isDisplayed() && opt.findElement(By.cssSelector("input")).isSelected()
                            && !opt.getText().equalsIgnoreCase(Operator.BETSTONE.toString())
                            && opt.findElement(By.cssSelector("input")).isDisplayed())
                    .forEach(WebElement::click);
            LOGGER.info("Distribuidores definidos.");
        } catch (WebDriverException e) {
            LOGGER.fatal(e.getMessage());
        }
    }

    /**
     * Selecciona los inputs del combobox 'inpCountry_handler' donde se hace una comprobación del tipo de pais
     * para saber qué opción seleccionar. Al final, deselecciona todas aquellas opciones que pudieran estar
     * seleccionadas antes o que no empaten con el país.
     *
     * @param pais Objeto heredado de país
     */
    private void setCountry(Pais pais) {
        setWaitDriver(TIME_POLL);
        LOGGER.info("Inicio SetCountry");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"filter_335_MultiSelect\"]")));
        WebElement a  = driver.findElement(By.xpath("//*[@id=\"filter_335_MultiSelect\"]"));
        a.sendKeys(pais.getCountryType().toString());
        LOGGER.info("Pais definido " + pais.getCountryType().toString());
        }

    /**
     * Hace click en todos los 'inputs' que no correspondan.
     *
     * @param listWE Lista de WebElement correspondientes a las opciones de países.
     */
    private void deselectNoMatch(List<WebElement> listWE, Predicate predicate) {
        listWE.stream()
                .filter(predicate)
                .forEach(o -> ((WebElement) o).findElement(By.cssSelector("input")).click());
    }

    /**
     * Ingresa los operadores de casino en los elementos web de la página dependiendo el
     * país y el tipo de reporte.
     *
     * @param pais País a ingresar
     */
    private void setCasinoOperator(Pais pais) {
        try {
            casinoOperatorWE = wait.until(webDriver -> driver.findElement(By.id(inpCasinoOperator_handlerId)));
            casinoOperatorWE.click();
            List<WebElement> casinoOperatorsLisWE = driver.findElements(By.cssSelector(inpCasinoOperatorListId));

            casinoOperatorsLisWE.stream()
                    .filter(CasinoOperatorsPredicates.isCasinoOperatorsFromCountryNotSelected(pais))
                    .forEach(li -> li.findElement(By.cssSelector("input")).click());
            LOGGER.info("Prueba.");
            /*deselectNoMatch(casinoOperatorsLisWE, CasinoOperatorsPredicates.isCasinoOperatorsNotFromCountrySelected(pais));
            if (casinoOperatorsLisWE.stream()
                    .filter(li -> li.findElement(By.cssSelector("input")).isSelected())
                    .collect(Collectors.toList()).size() == 0) {
                refreshButton();
                setCasinoOperator(pais);
            }*/
            LOGGER.info("Operadores de Casino definidos.");
        } catch (Exception e) {
            LOGGER.warn("Error al seleccionar operadores de casino, reintentando...");
            refreshButton();
            setCasinoOperator(pais);
        }
    }

    /**
     * Ejecuta una selección de operadores de casino del país ingresado, pero de forma segmentada.
     *
     * @param pais País a buscar.
     */
    private void setCasinoOperatorSegmented(Pais pais) {
        try {
            casinoOperatorWE = wait.until(webDriver -> driver.findElement(By.id("inpCasinoOperator_handler")));
            casinoOperatorWE.click();
            List<WebElement> casinoOperatorsLisWE = driver.findElements(By.cssSelector(inpCasinoOperatorListId));
            casinoOperatorsLisWE.stream()
                    .filter(CasinoOperatorsPredicates.isCasinoOperatorsFromCountryNotSelectedFromSegment(pais, segmentCasinos))
                    .forEach(li ->
                            li.findElement(By.cssSelector("input")).click());
            LOGGER.info("Prueba.");
            deselectNoMatch(casinoOperatorsLisWE, CasinoOperatorsPredicates.isCasinoOperatorsNotFromCountrySelectedFromSegment(pais,
                    segmentCasinos));
            if (casinoOperatorsLisWE.stream()
                    .filter(li -> li.findElement(By.cssSelector("input")).isSelected())
                    .collect(Collectors.toList()).size() == 0) {
                refreshButton();
                setCasinoOperatorSegmented(pais);
            }

            LOGGER.info("Operadores de Casino definidos.");
        } catch (Exception e) {
            LOGGER.warn("Error al seleccionar operadores de casino, reintentando...");
            refreshButton();
            setCasinoOperatorSegmented(pais);
        }
    }


    /**
     * Hace click en el botón refresh de la página de scorecard.
     */
    private void refreshButton() {
        setWaitDriver(TIME_POLL);
        refreshWE = driver.findElement(By.xpath("//button[contains(.,'directions_run')]"));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();",refreshWE);
        setWaitDriver(TIME_POLL);
    }


    /**
     * Hace click en el botón del reporte seleccionado.
     *
     * @param reportType ReportType del reporte.
     */
    private void openReport(Pais pais, ReportType reportType) {
        switch (reportType) {
            case ALL_GAME_PROFIT:
                if (pais instanceof Nepal) {
                    wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"tab-content-1\"]/div/reports-tab/md-content/div[4]/div/div/div[2]/a[3]"))).click();
                }
                else{
                    wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"tab-content-1\"]/div/reports-tab/md-content/div[4]/div/div/div[2]/a[2]"))).click();
                }
                break;
            case SCORECARD_EGM:
                if (pais instanceof Nepal) {
                    wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"tab-content-1\"]/div/reports-tab/md-content/div[3]/div/div/div[2]/a[3]"))).click();
                }
                else{
                    wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"tab-content-1\"]/div/reports-tab/md-content/div[3]/div/div/div[2]/a[2]"))).click();
                }
                break;
            case MYSTERY:
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"tab-content-1\"]/div/reports-tab/md-content/div[6]/div/div/div[2]/a[2]"))).click();
                break;
        }
        LOGGER.info("Reporte solicitado.");
    }

    /**
     * Hace click en el botón del reporte seleccionado.
     *
     * @param reportType
     */
    private void openQuickReport(ReportType reportType) {
        switch (reportType) {
            case ALL_GAME_PROFIT:
            case SCORECARD_EGM:
                driver.findElement(By.cssSelector(quickEGM_GPWE)).click();
                break;
            case MYSTERY:
                misteryReportWE = (username.contains("gabriel")) ?
                        "#dtReportLayouts #colLayout_Row3" :
                        "#dtReportLayouts #colLayout_Row6";
                driver.findElement(By.cssSelector(misteryReportWE)).click();
                break;
        }
        LOGGER.info("Reporte solicitado.");
    }

    /**
     * Una vez click en el reporte especificado, espera a que cargue
     */
    private void Wait_for_report(ReportType reportType) throws InterruptedException {

        tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(1));
        if (reportType == ReportType.ALL_GAME_PROFIT) {
            try {
                setWaitDriver(TIME_POLL);

                wait.until(webDriver -> driver.findElement(By.xpath("//*[@id=\"addViewButton\"]")));
            }
            catch(TimeoutException e){
                Thread.sleep(25000);
                wait.until(webDriver -> driver.findElement(By.xpath("//*[@id=\"addViewButton\"]")));
            }
            finally {
                setWaitDriver(TIME_POLL);
                wait.until(webDriver -> driver.findElement(By.xpath("//*[@id=\"addViewButton\"]")));
            }
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"addViewButton\"]")));
        LOGGER.info("Reporte cargado");
    }

    private void downloadExcel(ReportType reportType) {
        setWaitDriver(TIME_POLL);
        WebElement downloadIcon = driver.findElement(By.xpath(imgTableExportId));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();",downloadIcon);
        setWaitDriver(TIME_POLL);
        LOGGER.info("Iniciando descarga");
        String downloadButton = (formatExcel) ?
                exportExcelButtonId :
                exportCsvButtonId;
        LOGGER.info(downloadButton);
        downloadWE = wait.until(driver -> driver.findElement(By.xpath(downloadButton)));
        setWaitDriver(TIME_POLL);
        js.executeScript("arguments[0].click();",downloadWE);
        setWaitDriver(TIME_POLL);
        LOGGER.info("Archivo solicitado, esperando descarga...");

    }

    private void CargaReporte(ReportType reportType) {

        wait.until(webDriver -> driver.findElement(By.xpath("//*[@id=\"borderLayout_eGridPanel\"]/div[1]/div/div[1]/div[3]/div/div/div[1]/div/div[4]")));
        LOGGER.info("Verificando si hay información");
        setWaitDriver(TIME_POLL);
        
    }

    /**
     * Checa del reporte cargado el total de la sumatoria de la variable Total In si son los reportes ScorecardEGM
     * y GameProfit, o el total de la sumatoria de la variable GGR si es el reporte Mystery.
     *
     * @param reportType
     * @return
     */
    private boolean verifyReportLoadedCorrectly(ReportType reportType) {
        setWaitDriver(TIME_POLL);
        switch (reportType) {
            case ALL_GAME_PROFIT:
            case SCORECARD_EGM:
                try {
                    wait.until(webDriver -> driver.findElement(By.xpath("//*[@id=\"scrollTableAggregateFooter\"]/div[9]")));
                    amount = driver.findElement(By.xpath("//*[@id=\"column_TotalWagers_uiAggregate\"]/.//span[text()]")).getAttribute("innerHTML");
                    LOGGER.info("Total In: $" +amount);
                    LOGGER.info("Reporte cargado correctamente");
            }   catch (Exception e) {
                    wait.until(webDriver -> driver.findElement(By.xpath("//*[@id=\"scrollTableAggregateFooter\"]/div[9]")));
                    amount = driver.findElement(By.xpath("//*[@id=\"column_TotalWagers_uiAggregate\"]/.//span[text()]")).getAttribute("innerHTML");
                    LOGGER.info("Total In: $" +amount);
                    LOGGER.info("Reporte cargado correctamente");
            }
                break;

            case MYSTERY:
                try {
                    wait.until(webDriver -> driver.findElement(By.xpath("//*[@id=\"scrollTableAggregateFooter\"]/div[4]")));
                    amount = driver.findElement(By.xpath("//*[@id=\"column_BonusWinAmount_uiAggregate\"]/.//span[text()]")).getAttribute("innerHTML");
                    LOGGER.info("Bonus Win Amount: $" +amount);
                    LOGGER.info("Reporte cargado correctamente");
                }   catch (Exception e) {
                    LOGGER.info("Reporte vacío");
                    amount = null;
                }
        }
        /**!(amount.isEmpty() || amount == null)*/
        return !(amount.isEmpty() || amount == null);
    }


    /**
     * Define el estado de la variable booleana daily. Esta variable permite saber a los procesos que se ejecuten si
     * las operaciones y descargas son con respecto a las tablas 'daily', si es true, o 'invoicing' si es false.
     *
     * @param daily Valor booleano a asignar a Daily.
     */
    public void setDaily(boolean daily) {
        this.daily = daily;
    }

    /**
     * Retorna el estado de la variable booleana daily.
     *
     * @return Valor booleano
     */
    public boolean isDaily() {
        return this.daily;
    }

    /**
     * Destruye la instancia actual del driver de Chrome, y asigna el objeto a nulo.
     */
    public void cancelProcess() {
        driver.quit();
        driver = null;
    }

    /**
     * Devuelve el valor de la variable booleano formatExcel.
     *
     * @return Valor booleano
     */
    public boolean isFormatExcel() {
        return formatExcel;
    }

    /**
     * Define el valor de la variable global booleana formatExcel. Esta variable permite ejecutar las operaciones y
     * descargas con archivos Excel, si es true, o con archivos CSV, si es false.
     *
     * @param formatExcel
     */
    public void useFormatExcel(boolean formatExcel) {
        this.formatExcel = formatExcel;
    }
}
