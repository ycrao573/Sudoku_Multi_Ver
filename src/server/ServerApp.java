package server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import shared.Puzzle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ServerApp extends Application implements ServerLogger {
    private final static Pattern commandPattern = Pattern.compile("(\\w+) (\\d+) ?(.*)");
    private final static Pattern newGamePattern = Pattern.compile("new (easy|medium|hard|\\d+)");

    private TextArea logArea;
    private TextField commandField;

    private SessionHandler sessionHandler;

    @Override
    public void start(Stage primaryStage) {
        logArea = new TextArea();
        logArea.setEditable(false);

        commandField = new TextField();
        commandField.setOnAction(event -> {
            Matcher matcher = commandPattern.matcher(commandField.getText());
            if (matcher.matches()) {
                String command = matcher.group(1);
                int dest = Integer.parseInt(matcher.group(2));
                if (command.equals("message")) {
                    String message = matcher.group(3);
                    sessionHandler.sendMessage(dest, message);
                } else if (command.equals("moves")) {
                    sessionHandler.printMoves(dest);
                }
            }

//            matcher = newGamePattern.matcher(commandField.getText());
//            if (matcher.matches()) {
//                Puzzle[] puzzles = new Puzzle[2];
//                if (matcher.group(1) != null) {
//                    String difficulty = matcher.group(1);
//                    if (difficulty.equals("easy")) {
//                        for (int i = 0; i < 2; i++) {
//                            puzzles[i] =
//                        }
//                        Puzzle.randomEasy();
//                    } else if (difficulty.equals("medium")) {
//                        num = Puzzle.NUM_MEDIUM;
//                    } else if (difficulty.equals("hard")) {
//                        num = Puzzle.NUM_HARD;
//                    } else {
//                        num = Integer.parseInt(difficulty);
//                    }
//                }
//            }
            commandField.clear();
        });

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(new ScrollPane(logArea));
        borderPane.setBottom(commandField);

        Scene scene = new Scene(borderPane);
        primaryStage.setTitle("Sudoku Server");
        primaryStage.setScene(scene);
        primaryStage.show();

        sessionHandler = new SessionHandler(8000, this);
        sessionHandler.setGame(new Puzzle[] {
                Puzzle.randomEasy(),
                Puzzle.randomEasy()
        });
        sessionHandler.start();
    }

    public void write(String message) {
        Platform.runLater(() -> logArea.appendText(message + '\n'));
    }

    public void write(int i, String message) {
        Platform.runLater(() -> logArea.appendText(
                String.format("[Player %d] %s%n", i, message)));
    }
}
