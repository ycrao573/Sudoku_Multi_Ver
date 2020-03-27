package shared;

import java.io.Serializable;

public class CellData implements Serializable {
    private boolean[] marks = new boolean[Puzzle.SIZE];
    private boolean isEditable = true;
    private boolean changed;


    public static CellData newEmptyCell() {
        return new CellData();
    }
    public static CellData newStaticCell(int num) {
        CellData cellData = new CellData();
        cellData.marks[num - 1] = true;
        cellData.isEditable = false;

        return cellData;
    }


    public boolean isEditable() {
        return isEditable;
    }


    public boolean isChanged() {
        return changed;
    }

    public void clearChanged() {
        this.changed = false;
    }


    public boolean isEmpty() {
        return getMarkCount() == 0;
    }

    public void clearCell() {
        for (int i = 0; i < 9; i++) {
            marks[i] = false;
        }
        changed = true;
    }


    public boolean isNumber() {
        return getMarkCount() == 1;
    }

    public int getNumber() {
        if (isNumber()) {
            for (int i = 0; i < Puzzle.SIZE; i++) {
                if (marks[i]) return i + 1;
            }
        }

        return 0;
    }

    public void setNumber(int num) {
        clearCell();
        addMark(num);
        changed = true;
    }


    public boolean isMark() {
        return getMarkCount() > 1;
    }

    public int getMarkCount() {
        int count = 0;
        for (int i = 0; i < Puzzle.SIZE; i++) {
            if (marks[i]) count++;
        }
        return count;
    }

    public boolean[] getMarks() {
        return marks;
    }

    public void addMark(int num) {
        marks[num - 1] = true;
    }

    public void toggleMark(int num) {
        marks[num - 1] = !marks[num - 1];
        changed = true;
    }
}
