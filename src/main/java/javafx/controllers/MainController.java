package javafx.controllers;

import bigquery.BigQueryUploader;
import com.betstone.etl.ScorecardHandler;
import com.betstone.etl.enums.ReportType;
import com.betstone.etl.enums.SiteType;
import com.betstone.etl.io.PythonTransformation;
import com.betstone.etl.models.*;
import com.betstone.etl.process.ready.ProfitVerificator;
import com.betstone.etl.process.ready.jars.MacroExecuter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static com.betstone.etl.io.IOUtils.setPropertiesFile;
import static com.betstone.etl.io.IOUtils.setPropertiesValue;

public class MainController implements Initializable {


    @FXML
    private ComboBox<String> temporalCB;
    @FXML
    private ComboBox<String> modeCB;

    @FXML
    private CheckBox mexicoCheckB;
    @FXML
    private CheckBox laosCheckB;
    @FXML
    private CheckBox spainCheckB;
    @FXML
    private CheckBox nepalShangriCheckB;
    @FXML
    private CheckBox nepalNotShangriCheckB;
    @FXML
    private CheckBox turksCheckB;

    @FXML
    private CheckBox gameprofitCheckB;
    @FXML
    private CheckBox deviceEGMCheckB;
    @FXML
    private CheckBox misteryGGRCheckB;


    @FXML
    private DatePicker initDateDP;

    @FXML
    private TextField outputDirectoryTF;
    @FXML
    private TextField propertiesDirectoryTF;

    @FXML
    private Button cancelButton;
    @FXML
    private Button initButton;
    @FXML
    private Button cancelUploadButton;
    @FXML
    private Button initUploadButton;
    @FXML
    private Button macroButton;
    @FXML
    private Button pythonButton;
    @FXML
    private Button rectButton;
    @FXML
    private ToggleButton formatBtn;
    @FXML
    private ToggleButton formatBtn1;

    @FXML
    private TextArea loggerArea;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private ToggleButton universalButton;

    @FXML
    private VBox tableUploadVB;
    @FXML
    private ToggleButton dailyButton;


    ObservableList<String> temporalArray = FXCollections.observableArrayList("1 Día", "1 Semana", "1 Mes");
    ObservableList<String> modeArray = FXCollections.observableArrayList("Descarga", "Verificación");

    DirectoryChooser directoryChooser;
    Path outputPath;

    ScorecardHandler scorecardHandler;
    BigQueryUploader uploader;

    private Service serviceETLProcess;

    @FXML
    private Tooltip dateTooltip;
    private FileChooser fileChooser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        temporalCB.getItems().addAll(temporalArray);
        modeCB.getItems().addAll(modeArray);
        outputDirectoryTF.setText("Z:/Files_GamesProfit");
        outputDirectoryTF.setOnMouseClicked(e -> {
            openDirectoryChooser();
            outputDirectoryTF.setText(outputPath.toString());
        });

        initUploadButton.setOnAction(event -> startUploadProcess());

        tableUploadVB.disableProperty().bind(universalButton.selectedProperty());
        universalButton.setSelected(true);
        formatBtn1.selectedProperty().bindBidirectional(formatBtn.selectedProperty());


        propertiesDirectoryTF.setOnMouseClicked(event -> {
            openFileChooser();
            propertiesDirectoryTF.setText(outputPath.toString());
        });

        cancelButton.setOnAction(e -> cancelProcess());
        initButton.setOnAction(e -> startProcess());
        initDateDP.setTooltip(dateTooltip);
        initDateDP.setOnMouseClicked(e -> showTooltip());
        initDateDP.setOnMouseEntered(e -> showTooltip());

        macroButton.setOnAction(e -> startMacro());
        pythonButton.setOnAction(e -> startPython());
        rectButton.setOnAction(e -> startRectifier());
        dailyButton.setOnAction(event -> {
            if (dailyButton.isSelected())
                dailyButton.setText("Daily");
            else
                dailyButton.setText("Weekly");
        });

        formatBtn.setOnAction(event -> {
            if (formatBtn.isSelected())
                formatBtn.setText("Excel");
            else
                formatBtn.setText("CSV");
        });
        formatBtn1.textProperty().bindBidirectional(formatBtn.textProperty());
        formatBtn1.setOnAction(e -> {
            if (formatBtn.isSelected())
                formatBtn.setText("Excel");
            else
                formatBtn.setText("CSV");
        });
    }

    private void startRectifier() {
        loggerArea.clear();
        loggerArea.appendText("Iniciando...");
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        serviceETLProcess = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        ProfitVerificator rectifier = new ProfitVerificator();
                        rectifier.rectificationProcess(formatBtn.isSelected());
                        return null;
                    }
                };
            }
        };
        serviceETLProcess.setOnSucceeded(event -> {
            loggerArea.appendText("Proceso Completado");
            progressBar.setProgress(0);
        });
        serviceETLProcess.setOnCancelled(event -> loggerArea.appendText("Proceso Fallido"));
        serviceETLProcess.restart();
    }

    private void startMacro() {
        loggerArea.clear();
        loggerArea.appendText("Iniciando...");
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        serviceETLProcess = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        MacroExecuter macroExecuter = new MacroExecuter();
                        macroExecuter.execute(scorecardHandler);
                        return null;

                    }
                };
            }
        };
        serviceETLProcess.setOnSucceeded(event -> {
            loggerArea.setText("Proceso Completado");
            progressBar.setProgress(0);
        });
        serviceETLProcess.setOnCancelled(event -> {
            loggerArea.appendText("Proceso Fallido");
            progressBar.setProgress(0);
        });
        serviceETLProcess.restart();
    }

    private void startPython() {
        loggerArea.clear();
        loggerArea.appendText("Iniciando...");
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        serviceETLProcess = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        PythonTransformation py = new PythonTransformation();
                        py.execute();
                        return null;

                    }
                };
            }
        };
        serviceETLProcess.setOnSucceeded(event -> {
            loggerArea.setText("Proceso Completado");
            progressBar.setProgress(0);
        });
        serviceETLProcess.setOnCancelled(event -> {
            loggerArea.appendText("Proceso Fallido");
            progressBar.setProgress(0);
        });
        serviceETLProcess.restart();
    }

    private void startUploadProcess() {
        loggerArea.clear();
        loggerArea.appendText("Iniciando ");
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        serviceETLProcess = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        if (universalButton.isSelected()) {
                            uploader = new BigQueryUploader();
                            uploader.uploadBigQueryFiles();
                        } else {

                        }
                        return null;

                    }
                };
            }
        };
        serviceETLProcess.setOnSucceeded(event -> {
            loggerArea.setText("Proceso Completado");
            progressBar.setProgress(0);
        });
        serviceETLProcess.setOnCancelled(event -> {
            loggerArea.appendText("Proceso Fallido");
            progressBar.setProgress(0);
        });
        serviceETLProcess.restart();
    }

    private void disableUpload() {
        tableUploadVB.setDisable(!tableUploadVB.isDisabled());
    }

    private void showTooltip() {
        dateTooltip.setText("Máximo, un día anterior a la actual. Temporalidad de Semana o " +
                "mes es hacia atrás a partir de la fecha seleccionada.");
    }


    private void startProcess() {
        loggerArea.clear();
        loggerArea.appendText("Iniciando...\n");
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        serviceETLProcess = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Object call() throws Exception {
                        if (!isValidDate()) {
                            cancel();
                            return null;
                        }
                        loggerArea.appendText("En ejecución...\n");
                        List<Pais> paisList = buildPaisList();
                        List<ReportType> reportTypes = buildReportTypes();
                        outputPath = Paths.get(outputDirectoryTF.getText());
                        switch (modeArray.get(modeCB.getSelectionModel().getSelectedIndex())) {
                            case "Descarga":
                                downloadProcess(paisList, reportTypes, outputPath);
                                break;
                            case "Verificación":
                                verificationProcess(paisList, reportTypes);
                                break;
                            default:
                                loggerArea.setStyle("-fx-font-style: red");
                                loggerArea.appendText("No se ha seleccionado apropiadamente\n");
                        }
                        return null;

                    }
                };
            }
        };
        serviceETLProcess.setOnSucceeded(event -> {
            loggerArea.appendText("Proceso Completado");
            progressBar.setProgress(0);
        });
        serviceETLProcess.setOnCancelled(event -> {
            loggerArea.appendText("Proceso Fallido\n");
            progressBar.setProgress(0);
        });
        serviceETLProcess.restart();
    }

    private void verificationProcess(List<Pais> paisList, List<ReportType> reportTypes) {
        ProfitVerificator verify = new ProfitVerificator();
        verify.setPrintWriter();
        switch (temporalArray.get(temporalCB.getSelectionModel().getSelectedIndex())) {
            case "1 Día":
                verify.oneDayVerificationList(paisList, reportTypes, dailyButton.isSelected());
                break;
            case "1 Semana":
                verify.oneWeekVerificationList(paisList, reportTypes, dailyButton.isSelected());
                break;
            case "1 Mes":
                verify.oneMonthVerificationList(paisList, reportTypes, dailyButton.isSelected());
                break;
            default:
                loggerArea.appendText("No se ha seleccionado temporalidad\n");
                break;
        }
        verify.getPrintWriter().close();
        loggerArea.appendText("Toda verificación se guarda en el archivo \"resultsBadDays.txt\" de los recursos del aplicativo.\n");
    }

    private void downloadProcess(List<Pais> paisList, List<ReportType> reportTypes, Path path) {
        ScorecardHandler scorecardHandler = new ScorecardHandler();
        try {
            if (!propertiesDirectoryTF.getText().isEmpty())
                setPropertiesFile(propertiesDirectoryTF.getText());
            else
                setPropertiesFile("config.properties");
            setPropertiesValue("output.directory", path.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        scorecardHandler.setDaily(dailyButton.isSelected());
        scorecardHandler.useFormatExcel(formatBtn.isSelected());
        switch (temporalArray.get(temporalCB.getSelectionModel().getSelectedIndex())) {
            case "1 Día":
                scorecardHandler.oneDayDownloadCountryList(paisList, reportTypes);
                break;
            case "1 Semana":
                scorecardHandler.oneWeekDownloadCountryList(paisList, reportTypes);
                break;
            case "1 Mes":
                scorecardHandler.oneMonthDownloadCountryList(paisList, reportTypes);
                break;
            default:
                loggerArea.appendText("No se ha seleccionado temporalidad");
                break;
        }
    }

    private boolean isValidDate() {
        if (isDatePickerNull()) {
            loggerArea.appendText("No se ha escogida fecha alguna.\n");
            return false;
        } else if (isDatePickerTodayOrHigher()) {
            loggerArea.setStyle("-fx-text-fill: red");
            loggerArea.appendText("No se puede escoger la fecha actual, máximo un día anterior.\n");
            return false;
        }
        return true;
    }

    private boolean isDatePickerNotValidForVerification() {
        if (modeCB.getSelectionModel().getSelectedIndex() == 0)
            return false;
        LocalDate selected = initDateDP.getValue(),
                lastSunday = LocalDate.now();
        while (lastSunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
            lastSunday = lastSunday.minusDays(1);
        }
        return (selected.isAfter(lastSunday));
    }

    private boolean isDatePickerTodayOrHigher() {
        return (initDateDP.equals(LocalDate.now())) || (initDateDP.getValue().isAfter(LocalDate.now()));
    }

    private boolean isDatePickerNull() {
        return initDateDP.getEditor().getText().isEmpty();
    }

    private List<ReportType> buildReportTypes() throws Exception {
        List<ReportType> reportTypes = new ArrayList<>();
        if (gameprofitCheckB.isSelected()) reportTypes.add(ReportType.ALL_GAME_PROFIT);
        if (deviceEGMCheckB.isSelected()) reportTypes.add(ReportType.SCORECARD_EGM);
        if (misteryGGRCheckB.isSelected()) reportTypes.add(ReportType.MYSTERY);
        if (reportTypes.size() == 0) {
            String message = "No hay reportes seleccionados\n";
            loggerArea.appendText(message);
            ScorecardHandler.LOGGER.warn(message);
            throw new Exception();
        }
        return reportTypes;
    }

    private List<Pais> buildPaisList() throws Exception {
        List<Pais> paisList = new ArrayList<>();
        if (mexicoCheckB.isSelected()) paisList.add(new Mexico(initDateDP.getValue()));
        if (laosCheckB.isSelected()) paisList.add(new Laos(initDateDP.getValue()));
        if (spainCheckB.isSelected()) paisList.add(new Spain(initDateDP.getValue()));
        if (nepalShangriCheckB.isSelected()) paisList.add(new Nepal(initDateDP.getValue(), SiteType.SHANGRI));
        if (nepalNotShangriCheckB.isSelected()) paisList.add(new Nepal(initDateDP.getValue(), SiteType.TIGER_PALACE));
        if (turksCheckB.isSelected()) paisList.add(new Thurks(initDateDP.getValue()));
        if (paisList.size() == 0) {
            String message = "No hay paises seleccionados\n";
            ScorecardHandler.LOGGER.warn(message);
            loggerArea.appendText(message);
            throw new Exception();
        }
        return paisList;
    }

    private void openDirectoryChooser() {
        directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        directoryChooser.setTitle("Escoja un directorio de salida para los archivos descargados.");
        try {
            outputPath = directoryChooser.showDialog(new Stage()).toPath();
            if (outputPath == null)
                outputPath = Paths.get("Z:/Files_GamesProfit");
        } catch (Exception e) {
            loggerArea.appendText("Error al seleccionar directorio.\n");
        }
    }

    private void openFileChooser() {
        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle("Seleccione el archivo .properties");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Arhivo Properties", ".properties"));
        try {
            outputPath = fileChooser.showOpenDialog(new Stage()).toPath();
        } catch (Exception e) {
            loggerArea.appendText("Error al seleccionar archivo .properties.\n");
        }
    }

    private void cancelProcess() {
        try {
            if (serviceETLProcess.isRunning()) {
                scorecardHandler.cancelProcess();
                serviceETLProcess.cancel();
            }
        } catch (NullPointerException e) {
            loggerArea.appendText("No hay ningún proceso activo.");
        }
    }
}
