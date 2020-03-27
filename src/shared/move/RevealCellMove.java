package shared.move;

import shared.GameData;

public class RevealCellMove extends BoardMove {
    public RevealCellMove(int row, int col) {
        super(row, col);
    }

    @Override
    public void move(GameData gameData) {
        gameData.revealCellCount++;
        gameData.board[row][col].setNumber(gameData.puzzle.solution[row][col]);
    }

    @Override
    public boolean valid(GameData gameData) {
        return super.valid(gameData) && gameData.revealCellCount < gameData.maxRevealCell;
    }

    @Override
    public String toString() {
        return String.format("Reveal cell (%d, %d)", row, col);
    }
}
