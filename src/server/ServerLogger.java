package server;

interface ServerLogger {
    void write(String message);
    void write(int i, String message);
}
