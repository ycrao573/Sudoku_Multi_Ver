package shared.move;

import shared.GameData;

import java.util.Random;

public class ShuffleCellsAction extends Move {
    @Override
    public void move(GameData oppGameData) {
        oppGameData.shuffleCellsCount++;

        Random random = new Random(time);

        int count = 0;
        while (count < oppGameData.numShuffle) {
            int row = random.nextInt(9);
            int col = random.nextInt(9);
            int num = random.nextInt(9) + 1;

            if (oppGameData.board[row][col].isEditable()) {
                oppGameData.board[row][col].setNumber(num);
                count++;
            }
        }
    }

    @Override
    public boolean valid(GameData oppGameData) {
        return oppGameData.shuffleCellsCount < oppGameData.maxShuffleCells;
    }

    @Override
    public String toString() {
        return "Shuffle cells using seed " + time;
    }
}
