package shared.move;

import shared.GameData;

import java.io.Serializable;

public abstract class Move implements Serializable {
    protected long time = System.currentTimeMillis();

    public abstract void move(GameData gameData);

    public abstract boolean valid(GameData gameData);

    public long getTime() {
        return time;
    }
}
