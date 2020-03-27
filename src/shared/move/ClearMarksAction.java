package shared.move;

import shared.CellData;
import shared.GameData;

public class ClearMarksAction extends Move {
    @Override
    public void move(GameData oppGameData) {
        oppGameData.clearMarksCount++;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                CellData cellData = oppGameData.board[i][j];
                if (cellData.isMark()) {
                    cellData.clearCell();
                }
            }
        }
    }

    @Override
    public boolean valid(GameData oppGameData) {
        return oppGameData.clearMarksCount < oppGameData.maxClearMarks;
    }

    @Override
    public String toString() {
        return "Clear all marks";
    }
}
