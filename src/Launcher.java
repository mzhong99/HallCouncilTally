import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;

public class Launcher extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hall Council Tally");
        primaryStage.setMinWidth(400);

        VBox box = new VBox(6);

        VotingFileSelector dataFileSelector = new VotingFileSelector("Data File", "Select Data File");
        VotingFileSelector metadataFileSelector = new VotingFileSelector("Metadata File", "Select Metadata File");

        Button tempButton = new Button("Calculate Results");
        tempButton.setMaxWidth(Double.MAX_VALUE);
        tempButton.setOnAction(event -> {
            File dataFile = dataFileSelector.getFileChosen();
            File metaDataFile = metadataFileSelector.getFileChosen();

            if (dataFile == null || metaDataFile == null) {
                Stage alertStage = new Stage();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Missing Files");
                alert.setHeaderText("Data Files Missing!");
                alert.setContentText("Ensure that data and metadata files have been selected.");
                alert.showAndWait();
            }
            else {
                VotingTallyDisplay votingTallyDisplay = new VotingTallyDisplay(dataFile, metaDataFile);
                votingTallyDisplay.show();
            }
        });

        box.getChildren().addAll(
                dataFileSelector.text,
                dataFileSelector.button,
                metadataFileSelector.text,
                metadataFileSelector.button,
                tempButton
        );

        StackPane fileChoosingPane = new StackPane();
        fileChoosingPane.getChildren().addAll(box);
        fileChoosingPane.setPadding(new Insets(6));

        primaryStage.setScene(new Scene(fileChoosingPane));
        primaryStage.show();
    }
}
