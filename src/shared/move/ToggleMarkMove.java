package shared.move;

import shared.GameData;

public class ToggleMarkMove extends BoardMove {
    private int num;

    public ToggleMarkMove(int row, int col, int num) {
        super(row, col);
        this.num = num;
    }

    @Override
    public String toString() {
        return String.format("Toggle mark (%d, %d) -> %d", row, col, num);
    }

    @Override
    public void move(GameData gameData) {
        gameData.board[row][col].toggleMark(num);
    }

    public int getNum() {
        return num;
    }
}
