package shared;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Scanner;

public class Puzzle implements Serializable {
    public final static int SIZE = 9;
    public final static int NUM_EASY = 60;
    public final static int NUM_MEDIUM = 45;
    public final static int NUM_HARD = 30;

    public int[][] solution = new int[SIZE][SIZE];
    public int[][] blank = new int[SIZE][SIZE];

    public static Puzzle fromPz(String filename) throws IOException {
        Scanner pz = new Scanner(new File(filename));

        Puzzle puzzle = new Puzzle();

        for (int i = 0; i < SIZE; i++) {
            String row = pz.nextLine();
            for (int j = 0; j < SIZE; j++) {
                puzzle.solution[i][j] = row.charAt(j) - '0';
            }
        }
        pz.nextLine();
        for (int i = 0; i < SIZE; i++) {
            String row = pz.nextLine();
            for (int j = 0; j < SIZE; j++) {
                puzzle.blank[i][j] = row.charAt(j) - '0';
            }
        }

        return puzzle;
    }

    public static Puzzle randomPuzzle(int num) {
        Puzzle puzzle = new Puzzle();

        PuzzleGenerator su = new PuzzleGenerator();
        su.setNum(num);
        su.genSudo();

        for (int i = 0; i < 9; i++) {
            System.arraycopy(su.data[i], 0, puzzle.blank[i], 0, 9);
        }

        su.solveSudo();
        for (int i = 0; i < 9; i++) {
            System.arraycopy(su.data[i], 0, puzzle.solution[i], 0, 9);
        }

        return puzzle;
    }

    public static Puzzle randomEasy() {
        return Puzzle.randomPuzzle(NUM_EASY);
    }

    public static Puzzle randomMedium() {
        return Puzzle.randomPuzzle(NUM_MEDIUM);
    }

    public static Puzzle randomHard() {
        return Puzzle.randomPuzzle(NUM_HARD);
    }
}

class PuzzleGenerator {
    int[][] data = new int[9][9];
    private int depth;
    private int num;

    PuzzleGenerator() {
        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                data[i][j] = 0;
            }
        }
    }

    void setNum(int num) {
        this.num = num;
    }

    void genSudo() {
        depth = 81 - 9;
        for (int i = 0; i < 9; ++i) {
            data[0][i] = i + 1;
        }
        for (int i = 0; i < 9; ++i) {
            int ta = (int) (Math.random() * 10) % 9;
            int tb = (int) (Math.random() * 10) % 9;
            int tem = data[0][ta];
            data[0][ta] = data[0][tb];
            data[0][tb] = tem;
        }
        for (int i = 0; i < 9; ++i) {
            int ta = (int) (Math.random() * 10) % 9;
            int tb = (int) (Math.random() * 10) % 9;
            int tem = data[0][i];
            data[0][i] = data[ta][tb];
            data[ta][tb] = tem;
        }
        solveSudo();//Ensure this sudoku is solvable
        depth = 81 - num;
        for (int i = 0; i < depth; ++i) {
            int ta = (int) (Math.random() * 10) % 9;
            int tb = (int) (Math.random() * 10) % 9;
            if (data[ta][tb] != 0)
                data[ta][tb] = 0;
            else
                i--;
        }
    }

    boolean solveSudo() {
        return dfs();
    }

    private int calcount(int r, int c, int[] mark) {
        for (int ti = 0; ti < 10; ++ti)
            mark[ti] = 0;
        for (int i = 0; i < 9; ++i) {
            mark[data[i][c]] = 1;
            mark[data[r][i]] = 1;
        }
        int rs = (r / 3) * 3;
        int cs = (c / 3) * 3;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                mark[data[rs + i][cs + j]] = 1;
            }
        }
        int count = 0;
        for (int i = 1; i <= 9; ++i) {
            if (mark[i] == 0)
                count++;
        }
        return count;
    }

    private boolean dfs() {
        if (depth == 0)
            return true;
        int mincount = 10;
        int mini = 0, minj = 0;
        int[] mark = new int[10];

        for (int i = 0; i < 9; ++i) {
            for (int j = 0; j < 9; ++j) {
                if (data[i][j] != 0)
                    continue;

                int count = calcount(i, j, mark);
                if (count == 0)
                    return false;
                if (count < mincount) {
                    mincount = count;
                    mini = i;
                    minj = j;
                }
            }
        }

        calcount(mini, minj, mark);
        for (int i = 1; i <= 9; ++i) {
            if (mark[i] == 0) {
                data[mini][minj] = i;
                depth--;
                dfs();
                if (depth == 0)
                    return true;
                data[mini][minj] = 0;
                depth++;
            }
        }
        return true;
    }
}
