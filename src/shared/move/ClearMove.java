package shared.move;

import shared.GameData;

public class ClearMove extends BoardMove {
    public ClearMove(int row, int col) {
        super(row, col);
    }

    @Override
    public String toString() {
        return String.format("Clear (%d, %d)", row, col);
    }

    @Override
    public void move(GameData gameData) {
        gameData.board[row][col].clearCell();
    }

    @Override
    public boolean valid(GameData gameData) {
        return super.valid(gameData) && !gameData.board[row][col].isEmpty();
    }
}
