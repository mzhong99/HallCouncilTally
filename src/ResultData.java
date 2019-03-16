import java.util.List;

public class ResultData {

    private final List<String> victors;
    private final String log;

    public ResultData(List<String> victors, String log) {
        this.victors = victors;
        this.log = log;
    }
    public String getVictors() {
        StringBuilder builder = new StringBuilder();
        for (String victor : victors) {
            builder.append(victor);
            builder.append(" ");
        }
        return builder.toString();
    }

    public String getLog() {
        return log;
    }
}
