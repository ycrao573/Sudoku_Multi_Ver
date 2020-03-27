package shared.move;

import shared.GameData;

public abstract class BoardMove extends Move {
    int row, col;

    public BoardMove(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public boolean valid(GameData gameData) {
        return gameData.board[row][col].isEditable();
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
