package shared.move;

import shared.GameData;

public class SetNumberMove extends BoardMove {
    private int num;

    public SetNumberMove(int row, int col, int num) {
        super(row, col);
        this.num = num;
    }

    @Override
    public String toString() {
        return String.format("Set number (%d, %d) -> %d", row, col, num);
    }

    @Override
    public void move(GameData gameData) {
        gameData.board[row][col].setNumber(num);
    }

    @Override
    public boolean valid(GameData gameData) {
        return super.valid(gameData) && gameData.board[row][col].getNumber() != num;
    }

    public int getNum() {
        return num;
    }
}
