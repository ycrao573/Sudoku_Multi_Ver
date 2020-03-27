package client;

import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import shared.CellData;
import shared.GameData;

class CellPane extends StackPane {
    private GameData gameData;
    private int size;
    private int row, col;
    private boolean selectHighlight, numHighlight, gridHighlight, conflictHighlight;

    CellPane(GameData gameData, int size, int row, int col) {
        this.gameData = gameData;
        this.size = size;
        this.row = row;
        this.col = col;

        setMinSize(size, size);
        setPrefSize(size, size);

        update();
    }


    void setSelectHighlight() {
        selectHighlight = true;
    }

    void setNumHighlight() {
        numHighlight = true;
    }

    void setGridHighlight() {
        gridHighlight = true;
    }

    void setConflictHighlight() {
        conflictHighlight = true;
    }

    void clearAllHighlights() {
        selectHighlight = false;
        numHighlight = false;
        gridHighlight = false;
        conflictHighlight = false;
    }


    void update() {
        CellData cellData = gameData.board[row][col];

        getStyleClass().clear();
        getStyleClass().add("cell");
        getStyleClass().add(String.format("grid-%d-%d", row / 3, col / 3));

        if (!cellData.isEditable()) {
            getStyleClass().add("static");
        }
        if (gameData.setting.gridHighlight && gridHighlight) {
            getStyleClass().add("grid-highlight");
        }
        if (cellData.isChanged()) {
            getStyleClass().add("change-highlight");
        }
        if (gameData.setting.numberHighlight && numHighlight) {
            getStyleClass().add("num-highlight");
        }
        if (gameData.setting.selectHighlight && selectHighlight) {
            getStyleClass().add("select-highlight");
        }
        if (gameData.setting.conflictHighlight && conflictHighlight) {
            getStyleClass().add("conflict-highlight");
        }

        getChildren().clear();
        if (cellData.isNumber()) {
            Text numberText = new Text();
            numberText.setText(String.valueOf(cellData.getNumber()));
            numberText.setFont(new Font(size * 0.5));

            getChildren().addAll(numberText);
        } else {
            GridPane gridPane = new GridPane();
            gridPane.setAlignment(Pos.CENTER);
            gridPane.setHgap(size * 0.2);

            boolean[] marks = cellData.getMarks();
            for (int i = 0; i < 9; i++) {
                Text markText = new Text();
                markText.setText(marks[i] ? String.valueOf(i + 1) : " ");
                markText.setFont(new Font(size * 0.25));

                gridPane.add(markText, i % 3, i / 3);
//                GridPane.setHalignment(markText, HPos.CENTER);
            }
            getChildren().addAll(gridPane);
        }
    }
}
