package client;

import javafx.scene.layout.GridPane;
import shared.GameData;

class BoardPane extends GridPane {
    private GameData gameData;

    private GridPane[] subGrids = new GridPane[9];
    private CellPane[][] cellPanes = new CellPane[9][9];

    BoardPane(GameData gameData, int cellSize) {
        this.gameData = gameData;

        getStyleClass().add("board-pane");

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cellPanes[i][j] = new CellPane(gameData, cellSize, i, j);
            }
        }

        for (int i = 0; i < 9; i++) {
            subGrids[i] = new GridPane();
            subGrids[i].getStyleClass().add(String.format("grid-%d-%d", i / 3, i % 3));
        }

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                subGrids[3 * (i / 3) + j / 3].add(
                        cellPanes[i][j], j % 3, i % 3);
            }
        }

        for (int i = 0; i < 9; i++) {
            add(subGrids[i], i % 3, i / 3);
        }

        update();
    }

    CellPane[][] getCellPanes() {
        return cellPanes;
    }


    void highlightCell(int row, int col, int num) {
        clearAllHighlights();

        highlightGrid(row, col);
        highlightAllNumber(num);
        highlightSelected(row, col);
        highlightConflicts(row, col);

        update();
    }

    void highlightCell(int row, int col) {
        highlightCell(row, col, gameData.board[row][col].getNumber());
    }


    private void highlightGrid(int row, int col) {
        // Same col
        for (int i = 0; i < 9; i++) {
            cellPanes[i][col].setGridHighlight();
        }

        // Same row
        for (int j = 0; j < 9; j++) {
            cellPanes[row][j].setGridHighlight();
        }

        // Same subGrid
        int startRow = row / 3 * 3, startCol = col / 3 * 3;
        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                cellPanes[i][j].setGridHighlight();
            }
        }
    }

    private void highlightAllNumber(int num) {
        if (num == 0) {
            return;
        }

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (gameData.board[i][j].getNumber() == num) {
                    cellPanes[i][j].setNumHighlight();
                }
            }
        }
    }

    private void highlightSelected(int row, int col) {
        cellPanes[row][col].setSelectHighlight();
    }

    private void highlightConflicts(int row, int col) {
        int num = gameData.board[row][col].getNumber();

        if (num == 0) {
            return;
        }

        boolean isConflict = false;

        // Same col
        for (int i = 0; i < 9; i++) {
            if (i != row && gameData.board[i][col].getNumber() == num) {
                cellPanes[i][col].setConflictHighlight();
                isConflict = true;
            }
        }

        // Same row
        for (int j = 0; j < 9; j++) {
            if (j != col && gameData.board[row][j].getNumber() == num) {
                cellPanes[row][j].setConflictHighlight();
                isConflict = true;
            }
        }

        // Same subGrid
        int startRow = row / 3 * 3, startCol = col / 3 * 3;
        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                if (i != row && j != col && gameData.board[i][j].getNumber() == num) {
                    cellPanes[i][j].setConflictHighlight();
                    isConflict = true;
                }
            }
        }

        if (isConflict) {
            cellPanes[row][col].setConflictHighlight();
        }
    }

    private void clearAllHighlights() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cellPanes[i][j].clearAllHighlights();
            }
        }
    }


    void update() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cellPanes[i][j].update();
            }
        }
    }
}
