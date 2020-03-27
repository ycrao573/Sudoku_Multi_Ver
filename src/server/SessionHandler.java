package server;

import shared.Callable;
import shared.CellData;
import shared.GameData;
import shared.Puzzle;
import shared.move.Move;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

class SessionHandler extends Thread {
    private static final int NUM_PLAYERS = 2;

    private ServerLogger logger;
    private int port;

    private Socket[] sockets = new Socket[NUM_PLAYERS];
    private ReentrantLock[] outStreamLocks = new ReentrantLock[NUM_PLAYERS];
    private ObjectOutputStream[] outStreams = new ObjectOutputStream[NUM_PLAYERS];
    private ObjectInputStream[] inStreams = new ObjectInputStream[NUM_PLAYERS];

    private Puzzle[] puzzle = new Puzzle[NUM_PLAYERS];
    private GameData[] gameData = new GameData[NUM_PLAYERS];
    private int[] opponent = new int[NUM_PLAYERS];

    long startTime;


    SessionHandler(int port, ServerLogger logger) {
        this.port = port;
        this.logger = logger;
    }

    void setGame(Puzzle[] puzzle) {
        this.puzzle = puzzle;

        for (int i = 0; i < NUM_PLAYERS; i++) {
            opponent[i] = (i + 1) % NUM_PLAYERS;
            gameData[i] = new GameData(puzzle[i]);
        }

        logger.write("New game created");
    }


    @Override
    public void run() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(port);
            logger.write("Socket opened on port " + port);
        } catch (IOException ex) {
            logger.write("Failed to open socket on port" + port);
            return;
        }

        for (int i = 0; i < NUM_PLAYERS;) {
            if (acceptPlayer(serverSocket, i)) {
                i++;
            }
        }

        try {
            serverSocket.close();
            logger.write("Socket closed");
        } catch (IOException ex) {
            logger.write("Socket failed to close");
            return;
        }

        startGame();
    }

    private boolean acceptPlayer(ServerSocket serverSocket, int i) {
        // Accept player's connection, then get output and input stream
        try {
            sockets[i] = serverSocket.accept();

            outStreamLocks[i] = new ReentrantLock(true);
            outStreams[i] = new ObjectOutputStream(sockets[i].getOutputStream());
            inStreams[i] = new ObjectInputStream(sockets[i].getInputStream());

            logger.write(i, "Connected");
        } catch (IOException ex) {
            logger.write(i, "Connection failed");
            return false;
        }

        // Create new thread to listen for player's command
        new Thread(() -> {
            while (!sockets[i].isClosed()) {
                try {
                    String command = inStreams[i].readUTF();

                    if (listeners.containsKey(command)) {
                        listeners.get(command).run(i);
                    } else {
                        logger.write(i, "Unknown command: " + command);
                    }
                } catch (IOException ex) {
                    logger.write(i,"Failed to receive command");
                }
            }
        }).start();
        
        return true;
    }

    private void startGame() {
        startTime = System.currentTimeMillis();

        for (int i = 0; i < NUM_PLAYERS; i++) {
            int player = i;

            gameData[player].startTime = startTime;
            syncGameData(player);

            sendWrapper(
                    "START", player,
                    () -> {
                        outStreams[player].writeLong(startTime);
                        outStreams[player].flush();
                    },
                    () -> logger.write(player, "Start game"),
                    () -> logger.write(player, "Failed to start game")
            );
        }
    }


    private void syncGameData(int i) {
        sendWrapper(
                "SYNC", i,
                () -> {
                    outStreams[i].reset();
                    outStreams[i].writeObject(gameData[i]);
                    outStreams[i].writeObject(gameData[opponent[i]]);
                },
                () -> logger.write(i, "Sent game data"),
                () -> logger.write(i, "Failed to send game data")
        );
    }

    private void sendMoveToOpp(int i, Move move) {
        int opp = opponent[i];
        sendWrapper(
                "OPP_MOVE", opp,
                () -> {
                    outStreams[opp].reset();
                    outStreams[opp].writeObject(move);
                    outStreams[opp].flush();
                },
                () -> logger.write(i, String.format("Sent move to %d: %s", opp, move)),
                () -> logger.write(i, String.format("Failed to send move to %d: %s", opp, move))
        );
    }

    private void sendActionToOpp(int i, Move action) {
        int opp = opponent[i];
        sendWrapper(
                "OPP_ACTION", opp,
                () -> {
                    outStreams[opp].reset();
                    outStreams[opp].writeObject(action);
                    outStreams[opp].flush();
                },
                () -> logger.write(i, String.format("Sent action to %d: %s", opp, action)),
                () -> logger.write(i, String.format("Failed to send action to %d: %s", opp, action))
        );
    }

    private void endGame(int winner) {
        for (int i = 0; i < NUM_PLAYERS; i++) {
            int player = i;
            boolean win = player == winner;
            sendWrapper(
                    win ? "WIN" : "LOSE", player,
                    () -> {
                        try {
                            outStreams[player].writeLong(System.currentTimeMillis() - startTime);
                            outStreams[player].flush();
                        } catch (IOException ex) {
                            logger.write(player,"Failed to send win condition");
                        }
                    },
                    () -> logger.write(player, win ? "Won" : "Lost"),
                    () -> logger.write(player, "Failed to end game")
            );
        }
    }

    private boolean hasWon(int player) {
        CellData[][] board = gameData[player].board;
        int[][] solution = puzzle[player].solution;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j].getNumber() != solution[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }


    void sendMessage(int i, String message) {
        sendWrapper(
                "MESSAGE", i,
                () -> {
                    outStreams[i].writeUTF(message);
                    outStreams[i].flush();
                },
                () -> logger.write(i, "Sent message: " + message),
                () -> logger.write(i, "Failed to send message: " + message)
        );
    }

    void printMoves(int i) {
        for (Move move : gameData[i].moves) {
            logger.write(i, String.format("%s %s",
                    gameData[i].timeElapsed(move),
                    move));
        }
    }


    private Map<String, ServerListener> listeners = Map.of(
            "MOVE", i -> {
                try {
                    Move move = (Move) inStreams[i].readObject();
                    gameData[i].doMove(move);
                    sendMoveToOpp(i, move);

                    if (hasWon(i)) {
                        endGame(i);
                    }

                    logger.write(i, move.toString());
                } catch (IOException | ClassNotFoundException ex) {
                    syncGameData(i);
                    logger.write(i, "Failed to receive move");
                }
            },
            "ACTION", i -> {
                try {
                    Move move = (Move) inStreams[i].readObject();
                    gameData[opponent[i]].doMove(move);
                    sendActionToOpp(i, move);

                    if (hasWon(opponent[i])) {
                        endGame(i);
                    }

                    logger.write(i, move.toString());
                } catch (IOException | ClassNotFoundException ex) {
                    syncGameData(i);
                    logger.write(i, "Failed to receive move");
                }
            },
            "DISCONNECT", i -> {
                try {
                    sockets[i].close();
                    logger.write(i, "Disconnect");
                } catch (IOException ex) {
                    logger.write(i, "Failed to disconnect");
                }
            }
    );


    private void sendWrapper(String command, int i, Callable send,
                             Callable onSuccess, Callable onFail) {
        boolean[] success = new boolean[1];

        Thread sendThread = new Thread(() -> {
            outStreamLocks[i].lock();
            try {
                // Send out command
                outStreams[i].writeUTF(command);
                outStreams[i].flush();

                // Execute
                send.call();

                success[0] = true;
            } catch (Exception ex) {
                success[0] = false;
            } finally {
                outStreamLocks[i].unlock();
            }
        });

        sendThread.start();
        new Thread(() -> {
            try {
                sendThread.join();
                if (success[0]) {
                    onSuccess.call();
                } else {
                    onFail.call();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}
