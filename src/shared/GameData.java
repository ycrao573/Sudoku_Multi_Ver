package shared;

import shared.move.*;

import java.io.Serializable;
import java.util.LinkedList;

public class GameData implements Serializable {
    public Setting setting;
    public Puzzle puzzle;

    public CellData[][] board = new CellData[Puzzle.SIZE][Puzzle.SIZE];
    public LinkedList<Move> moves = new LinkedList<>();

    public int maxClearMarks = 10, maxShuffleCells = 2, maxRevealCell = 5;
    public int numShuffle = 10;

    public int clearMarksCount, shuffleCellsCount, revealCellCount;

    public long startTime;


    public GameData() {
        this(new Setting());
    }

    public GameData(Setting setting) {
        this.setting = setting;
        this.puzzle = new Puzzle();

        for (int i = 0; i < Puzzle.SIZE; i++) {
            for (int j = 0; j < Puzzle.SIZE; j++) {
                board[i][j] = CellData.newEmptyCell();
            }
        }
    }

    public GameData(Puzzle puzzle) {
        this(new Setting(), puzzle);
    }

    public GameData(Setting setting, Puzzle puzzle) {
        this.setting = setting;
        this.puzzle = puzzle;

        // Fill in all CellData from Puzzle Blank
        for (int i = 0; i < Puzzle.SIZE; i++) {
            for (int j = 0; j < Puzzle.SIZE; j++) {
                int num = puzzle.blank[i][j];
                if (num == 0) {
                    board[i][j] = CellData.newEmptyCell();
                } else {
                    board[i][j] = CellData.newStaticCell(num);
                }
            }
        }
    }


    public void sync(GameData gameData) {
//        setting = gameData.setting;
        puzzle = gameData.puzzle;
        board = gameData.board;
        moves = gameData.moves;

        clearMarksCount = gameData.clearMarksCount;
        shuffleCellsCount = gameData.shuffleCellsCount;
        revealCellCount = gameData.revealCellCount;

        maxClearMarks = gameData.maxClearMarks;
        maxRevealCell = gameData.maxRevealCell;
        maxShuffleCells = gameData.maxShuffleCells;
        numShuffle = gameData.numShuffle;

        startTime = gameData.startTime;
    }

    public void doMove(Move move) {
        if (move.valid(this)) {
            clearAllChanged();
            move.move(this);
            moves.add(move);
        }
    }

    private void clearAllChanged() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j].clearChanged();
            }
        }
    }

    public long timeElapsed(Move move) {
        return move.getTime() - startTime;
    }


    public int getNumCount(int num) {
        int count = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j].getNumber() == num) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getEmptyCount() {
        return getNumCount(0);
    }

    public int getMarkCount() {
        int count = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j].isMark()) {
                    count++;
                }
            }
        }
        return count;
    }
}
