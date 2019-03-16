import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class VotingFileSelector {

    private File fileChosen;
    private String fieldText;

    public Text text = new Text();
    public Button button = new Button();

    public VotingFileSelector(String fieldText, String buttonText) {
        this.fieldText = fieldText;
        text.setText(fieldText);

        button.setText(buttonText);
        button.setMaxWidth(Double.MAX_VALUE);
        initButton();
    }

    private void initButton() {
        button.setOnAction(event -> {
            Stage promptStage = new Stage();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
            fileChosen = fileChooser.showOpenDialog(promptStage);
            if (fileChosen != null) {
                text.setText(fieldText + ": " + fileChosen.getName());
            }
        });
    }

    public File getFileChosen() {
        return fileChosen;
    }
}
