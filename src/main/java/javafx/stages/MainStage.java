package javafx.stages;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MainStage extends Application {

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/fxmls/mainScene.fxml"));
            primaryStage.setTitle("BI Portal WebScrapper Betstone");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/mainScene.css")
                    .toExternalForm());
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/betstoneIcon.jpg")));
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
