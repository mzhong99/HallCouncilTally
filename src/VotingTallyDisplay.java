import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;

public class VotingTallyDisplay {

    private static final String DEFAULT_CHOICE = "Total Results";
    private Stage resultsStage = new Stage();
    private TextArea output = new TextArea();

    private VotingTally votingTally;

    public VotingTallyDisplay(File dataFile, File metadataFile) {
        votingTally = new VotingTally(dataFile, metadataFile);
        output.setStyle("-fx-font-family: 'monospaced'");
        output.setPrefHeight(520);
        output.setEditable(false);
        init();
    }

    private void init() {
        StackPane mainPane = new StackPane();
        mainPane.setPadding(new Insets(6));
        VBox vBox = new VBox(6);

        ObservableList<String> dropDownChoices = FXCollections.observableArrayList();
        dropDownChoices.add(DEFAULT_CHOICE);
        dropDownChoices.addAll(votingTally.getPositions());

        ChoiceBox<String> choiceBox = new ChoiceBox<String>(dropDownChoices);
        choiceBox.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            String choice = choiceBox.getItems().get(newValue.intValue());
            StringBuilder builder = new StringBuilder();
            if (choice.equals(DEFAULT_CHOICE)) {
                builder.append("Total Results:\n");
                for (String position : votingTally.getPositions()) {
                    builder.append(position);
                    builder.append(" ");
                    builder.append(votingTally.getResultsFor(position).getVictors());
                    builder.append("\n");
                }
            }
            else {
                builder.append(votingTally.getResultsFor(choice).getLog());
                builder.append("\n");
                builder.append("Victory goes to:\n");
                builder.append(votingTally.getResultsFor(choice).getVictors());
            }
            output.setText(builder.toString());
        });

        choiceBox.setValue(DEFAULT_CHOICE);
        vBox.getChildren().addAll(choiceBox, output);
        mainPane.getChildren().add(vBox);

        resultsStage.setScene(new Scene(mainPane));
    }

    public void show() {
        resultsStage.setMinWidth(800);
        resultsStage.setMinHeight(600);
        resultsStage.setResizable(false);
        resultsStage.setTitle("Results of Election");
        resultsStage.show();
    }
}
