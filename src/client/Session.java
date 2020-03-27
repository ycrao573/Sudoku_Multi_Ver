package client;

import shared.Callable;
import shared.GameData;
import shared.move.Move;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

class Session extends Thread {
    private ClientApp clientApp;
    private ClientLogger logger;
    private boolean playing;

    private Socket socket;

    private ReentrantLock outStreamLock;
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;


    Session(ClientApp clientApp, ClientLogger logger) {
        this.clientApp = clientApp;
        this.logger = logger;
    }


    void connect(String host, int port) {
        // Accept connection from server, then get output and input stream
        try {
            socket = new Socket(host, port);

            outStreamLock = new ReentrantLock(true);
            outStream = new ObjectOutputStream(socket.getOutputStream());
            inStream = new ObjectInputStream(socket.getInputStream());

            logger.write(String.format("Successfully connected to %s on port %d", host, port));
        } catch (IOException e) {
            logger.write(String.format("Connection to %s on port %d failed", host, port));
            return;
        }

        // Create new thread for listening commands from server
        new Thread(() -> {
            while (!socket.isClosed()) {
                try {
                    String command = inStream.readUTF();

                    if (listeners.containsKey(command)) {
                        listeners.get(command).run();
                    } else {
                        logger.write("Unknown command: " + command);
                    }
                } catch (IOException ex) {
                    logger.write("Failed to receive command");
                }
            }
            socket = null;
        }).start();
    }

    boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    void sendMove(Move move) {
        gameWrapper(
                "MOVE",
                () -> {
                    outStream.reset();
                    outStream.writeObject(move);
                    outStream.flush();
                },
                () -> logger.write("Sent move: " + move.toString()),
                () -> logger.write("Failed to send move: " + move.toString())
        );
    }

    void sendAction(Move action) {
        gameWrapper(
                "ACTION",
                () -> {
                    outStream.reset();
                    outStream.writeObject(action);
                    outStream.flush();
                },
                () -> logger.write("Sent action: " + action.toString()),
                () -> logger.write("Failed to send action: " + action.toString())
        );
    }

    void disconnect() {
        sendWrapper(
                "DISCONNECT",
                () -> socket.close(),
                () -> logger.write("Disconnected"),
                () -> logger.write("Failed to disconnect")
        );
    }


    private Map<String, ClientListener> listeners = Map.of(
            "SYNC", () -> {
                try {
                    GameData gameData = (GameData) inStream.readObject();
                    GameData oppGameData = (GameData) inStream.readObject();

                    clientApp.sync(gameData, oppGameData);

                    logger.write("Syncing game data");
                } catch (IOException | ClassNotFoundException ex) {
                    logger.write("Failed to sync game data");
                }
            },
            "START", () -> {
                try {
                    long startTime = inStream.readLong();

                    playing = true;
                    clientApp.startGame(startTime);

                    logger.write("Start game");
                } catch (IOException ex) {
                    logger.write("Failed to start game");
                }
            },
            "OPP_MOVE", () -> {
                try {
                    Move move = (Move) inStream.readObject();

                    clientApp.doOppMove(move);

                    logger.write("Opponent move: " + move);
                } catch (IOException | ClassNotFoundException ex) {
                    logger.write("Failed to receive opponent's move");
                }
            },
            "OPP_ACTION", () -> {
                try {
                    Move action = (Move) inStream.readObject();

                    clientApp.doOppAction(action);

                    logger.write("Opponent action: " + action);
                } catch (IOException | ClassNotFoundException ex) {
                    logger.write("Failed to receive opponent's action");
                }
            },
            "MESSAGE", () -> {
                try {
                    logger.write("[Server] " + inStream.readUTF());
                } catch (IOException ex) {
                    logger.write("Failed to receive message");
                }
            },
            "WIN", () -> {
                try {
                    long elapsed = inStream.readLong();
                    playing = false;
                    clientApp.gameWon(elapsed);
                    logger.write("Win");
                } catch (IOException ex) {
                    logger.write("Failed to receive win condition");
                }
            },
            "LOSE", () -> {
                try {
                    long elapsed = inStream.readLong();
                    playing = false;
                    clientApp.gameLost(elapsed);
                    logger.write("Lose");
                } catch (IOException ex) {
                    logger.write("Failed to receive win condition");
                }
            }
    );


    private void sendWrapper(String command, Callable send,
                             Callable onSuccess, Callable onFail) {
        if (!isConnected()) {
            logger.write("Not connected to server");
            return;
        }

        // [TODO] Hacky way to retrieve value from thread, maybe use Callable and Future
        boolean[] success = new boolean[1];

        Thread sendThread = new Thread(() -> {
            outStreamLock.lock();
            try {
                // Send out command
                outStream.writeUTF(command);
                outStream.flush();

                // Execute
                send.call();

                success[0] = true;
            } catch (Exception ex) {
                success[0] = false;
            } finally {
                outStreamLock.unlock();
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

    private void gameWrapper(String command, Callable send,
                             Callable onSuccess, Callable onFail) {
        if (!playing) {
            logger.write("Not in game");
            return;
        }

        sendWrapper(command, send, onSuccess, onFail);
    }
}
