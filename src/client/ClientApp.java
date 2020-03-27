package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import shared.GameData;
import shared.move.*;

public class ClientApp extends Application implements ClientLogger {
    public GameData gameData = new GameData();
    public GameData oppGameData = new GameData();

    private BoardPane boardPane = new BoardPane(gameData, 50);
    private BoardPane oppBoardPane = new BoardPane(oppGameData, 20);

    private TextArea logArea = new TextArea();
    private Timer timer = new Timer();
    private NumGrid numGrid = new NumGrid();
    private SideBar sideBar = new SideBar();

    private int selectRow = 0, selectCol = 0;
    private boolean isPencil = false;

    private Session session = new Session(this, this);

    @Override
    public void start(Stage primaryStage) {
        // Add handlers for mouse click on cell
        addCellListeners();

        VBox vBox = new VBox(5, timer, boardPane);
        vBox.setAlignment(Pos.CENTER);
        BorderPane bp = new BorderPane(vBox);
        bp.setRight(sideBar);
        bp.setTop(new SudokuMenu());

        Scene scene = new Scene(bp);
        scene.getStylesheets().addAll("./res/cell.css", "./res/ui.css");
        scene.setOnKeyTyped(event -> {
            char c = event.getCharacter().charAt(0);
            if (Character.isDigit(c)) {
                changeCell(c - '0');
            } else {
                switch (c) {
                    case 'w':
                        selectRow = selectRow <= 0 ? 0 : selectRow - 1;
                        break;
                    case 's':
                        selectRow = selectRow >= 8 ? 8 : selectRow + 1;
                        break;
                    case 'a':
                        selectCol = selectCol <= 0 ? 0 : selectCol - 1;
                        break;
                    case 'd':
                        selectCol = selectCol >= 8 ? 8 : selectCol + 1;
                        break;
                }
                boardPane.highlightCell(selectRow, selectCol);
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> disconnect());
        primaryStage.setTitle("Sudoku v1.0 (Beta)");

        // Set the application icon.
        primaryStage.getIcons().add(new Image("/res/icon.png"));
        primaryStage.show();
    }


    void sync(GameData gameData, GameData oppGameData) {
        this.gameData.sync(gameData);
        this.oppGameData.sync(oppGameData);
        Platform.runLater(() -> {
            boardPane.update();
            oppBoardPane.update();
            numGrid.update();
            sideBar.update();
        });
    }

    void startGame(long startTime) {
        timer.start();
        gameData.startTime = startTime;
    }

    void doOppMove(Move move) {
        oppGameData.doMove(move);
        Platform.runLater(oppBoardPane::update);
    }

    void doOppAction(Move action) {
        gameData.doMove(action);
        Platform.runLater(boardPane::update);
    }

    void gameLost(long elapsed) {
        disconnect();
        Platform.runLater(() -> {
            timer.stop();
            long seconds = elapsed / 1000;
            Alert loseAlert = new Alert(Alert.AlertType.NONE,
                    String.format("You LOST!!!!\nYour opponent's time: %02d:%02d",
                            seconds / 60,
                            seconds % 60),
                    ButtonType.CLOSE);
            loseAlert.showAndWait();
        });
    }

    void gameWon(long elapsed) {
        disconnect();
        Platform.runLater(() -> {
            timer.stop();
            long seconds = elapsed / 1000;
            Alert winAlert = new Alert(Alert.AlertType.NONE,
                    String.format("Congratulations!!! You WON!!!!\nYour time: %02d:%02d",
                            seconds / 60,
                            seconds % 60),
                    ButtonType.CLOSE);
            winAlert.showAndWait();
        });
    }

    private void disconnect() {
        if (session.isConnected()) {
            session.disconnect();
        }
        if (timer.running) {
            timer.stop();
        }
    }


    private void addCellListeners() {
        CellPane[][] cellPanes = boardPane.getCellPanes();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                int row = i, col = j;
                cellPanes[i][j].setOnMouseClicked(event -> {
                    selectRow = row;
                    selectCol = col;
                    boardPane.highlightCell(row, col);
                });
            }
        }
    }

    private void changeCell(int num) {
        Move move = (num == 0) ? new ClearMove(selectRow, selectCol) :
                    (isPencil) ? new ToggleMarkMove(selectRow, selectCol, num) :
                                 new SetNumberMove(selectRow, selectCol, num);

        gameData.doMove(move);
        session.sendMove(move);

        boardPane.highlightCell(selectRow, selectCol, num);
        numGrid.update();
    }


    private class SudokuMenu extends MenuBar {
        private SudokuMenu() {
            Menu settingMenu = new Menu("Settings");
            settingMenu.getItems().addAll(getHighlightMenu(), getShowLog(), getDisconnect());

            getMenus().add(settingMenu);
        }

        private Menu getHighlightMenu() {
            CheckMenuItem sameNumbers = new CheckMenuItem("Same numbers");
            sameNumbers.setSelected(gameData.setting.numberHighlight);
            sameNumbers.setOnAction(event -> {
                gameData.setting.numberHighlight = sameNumbers.isSelected();
                boardPane.update();
            });

            CheckMenuItem conflict = new CheckMenuItem("Conflict");
            conflict.setSelected(gameData.setting.conflictHighlight);
            conflict.setOnAction(event -> {
                gameData.setting.conflictHighlight = conflict.isSelected();
                boardPane.update();
            });

            CheckMenuItem sameGrid = new CheckMenuItem("Same grid");
            sameGrid.setSelected(gameData.setting.gridHighlight);
            sameGrid.setOnAction(event -> {
                gameData.setting.gridHighlight = sameGrid.isSelected();
                boardPane.update();
            });

            CheckMenuItem selected = new CheckMenuItem("Selected");
            selected.setSelected(gameData.setting.gridHighlight);
            selected.setOnAction(event -> {
                gameData.setting.selectHighlight = selected.isSelected();
                boardPane.update();
            });

            Menu highlightMenu = new Menu("Highlight");
            highlightMenu.getItems().addAll(sameNumbers, conflict, sameGrid, selected);
            return highlightMenu;
        }

        private MenuItem getShowLog() {
            CheckMenuItem showLog = new CheckMenuItem("Show log");
            showLog.setSelected(gameData.setting.showLogArea);
            showLog.setOnAction(event -> {
                gameData.setting.showLogArea = showLog.isSelected();
                sideBar.update();
            });

            return showLog;
        }

        private MenuItem getDisconnect() {
            MenuItem disconnect = new MenuItem("Disconnect");
            disconnect.setOnAction(event -> disconnect());
            return disconnect;
        }
    }

    private class SideBar extends VBox {
        private SideBar() {
            getStyleClass().add("side-bar");
            update();
        }

        private void update() {
            getChildren().clear();

            if (gameData.setting.showLogArea) {
                getChildren().add(getLogPane());
            }
            getChildren().add(getServerPrompt());
            getChildren().add(oppBoardPane);
            getChildren().add(numGrid);
            getChildren().add(getActionPane());
        }

        private ScrollPane getLogPane() {
            logArea.getStyleClass().clear();
            logArea.getStyleClass().add("log-area");

            logArea.setEditable(false);
            logArea.setPrefColumnCount(30);
            logArea.setPrefRowCount(10);

            return new ScrollPane(logArea);
        }

        private HBox getServerPrompt() {
            HBox serverPrompt = new HBox();
            serverPrompt.getStyleClass().add("server-prompt");

            TextField hostInput = new TextField();
            hostInput.setPrefColumnCount(10);

            TextField portInput = new TextField();
            portInput.setPrefColumnCount(5);

            Button connectButton = new Button("Connect");
            connectButton.setOnAction(event -> {
                if (session.isConnected())
                    return;

                String portStr = portInput.getText();
                int port = portStr.matches("\\d+") ? Integer.parseInt(portStr) : 0;

                session.connect(hostInput.getText(), port);
            });

            serverPrompt.getChildren().addAll(new Label("Host:"), hostInput,
                                              new Label("Port:"), portInput,
                                              connectButton);

            return serverPrompt;
        }

        private HBox getActionPane() {
            HBox hb = new HBox();
            hb.getStyleClass().add("action-pane");

            ToggleButton pencilToggle = new ToggleButton();
            pencilToggle.getStyleClass().add("pencil-toggle");
            pencilToggle.setOnAction(event -> isPencil = !isPencil);

            Button clearCell = new Button();
            clearCell.getStyleClass().add("clear-cell");
            clearCell.setOnAction(event -> {
                Move move = new ClearMove(selectRow, selectCol);

                gameData.doMove(move);
                session.sendMove(move);

                boardPane.highlightCell(selectRow, selectCol);
                numGrid.update();
            });

            Button revealCell = new Button();
            revealCell.getStyleClass().add("reveal-cell");
            revealCell.setDisable(gameData.revealCellCount >= gameData.maxRevealCell);
            revealCell.setOnAction(event -> {
                Move move = new RevealCellMove(selectRow, selectCol);

                gameData.doMove(move);
                session.sendMove(move);

                boardPane.highlightCell(selectRow, selectCol);
                revealCell.setDisable(gameData.revealCellCount >= gameData.maxRevealCell);
                numGrid.update();
            });

            Button clearMark = new Button();
            clearMark.getStyleClass().add("clear-mark");
            clearMark.setDisable(oppGameData.clearMarksCount >= oppGameData.maxClearMarks);
            clearMark.setOnAction(event -> {
                Move action = new ClearMarksAction();

                oppGameData.doMove(action);
                session.sendAction(action);

                oppBoardPane.update();
                clearMark.setDisable(oppGameData.clearMarksCount >= oppGameData.maxClearMarks);
            });

            Button shuffle = new Button();
            shuffle.getStyleClass().add("shuffle");
            shuffle.setDisable(oppGameData.shuffleCellsCount >= oppGameData.maxShuffleCells);
            shuffle.setOnAction(event -> {
                Move action = new ShuffleCellsAction();

                oppGameData.doMove(action);
                session.sendAction(action);

                oppBoardPane.update();
                shuffle.setDisable(oppGameData.shuffleCellsCount >= oppGameData.maxShuffleCells);
            });

            hb.getChildren().addAll(pencilToggle, clearCell, revealCell, clearMark, shuffle);
            return hb;
        }
    }

    private class NumGrid extends GridPane {
        private Text[] counts = new Text[9];

        private NumGrid() {
            getStyleClass().add("num-grid");

            Button[] numButtons = new Button[9];
            for (int i = 0; i < 9; i++) {
                final int num = i + 1;

                numButtons[i] = new Button();
                numButtons[i].setOnAction(event -> changeCell(num));

                Text numText = new Text(10, 20, String.valueOf(num));
                numText.getStyleClass().add("num-text");

                counts[i] = new Text(18, 10, "");
                counts[i].getStyleClass().add("count-text");

                Pane pane = new Pane(numText, counts[i]);
                pane.setPrefWidth(30);
                pane.setPrefHeight(30);

                numButtons[i].setGraphic(pane);

                add(numButtons[i], i % 3, i / 3);
            }

            update();
        }

        private void update() {
            for (int i = 0; i < 9; i++) {
                int count = gameData.getNumCount(i + 1);

                counts[i].getStyleClass().clear();
                counts[i].getStyleClass().add("count-text");
                if (count > 9) {
                    counts[i].getStyleClass().add("exceed-limit");
                }
                counts[i].setText(String.valueOf(count));
            }
        }
    }

    private class Timer extends Label {
        boolean running;

        private Timer() {
            getStyleClass().add("timer");
            update(0);
        }

        private void start() {
            if (running) {
                return;
            }

            new Thread(() -> {
                running = true;
                while (running) {

                    update(System.currentTimeMillis() - gameData.startTime);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
        }

        private void stop() {
            running = false;
        }

        private void update(long millis) {
            long seconds = millis / 1000;
            long minute = seconds / 60;
            long second = seconds % 60;
            Platform.runLater(() -> setText(String.format("%02d:%02d", minute, second)));
        }
    }


    @Override
    public void write(String message) {
        Platform.runLater(() -> logArea.appendText(message + '\n'));
    }
}
