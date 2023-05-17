package ru.vixtor.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private final int activeConnectionThreads = 64;
    private int port = 9999;
    private int activeCount = 0;

    private Map<Query, Handler> map;

    private ServerSocket serverSocket;

    private final ExecutorService service = Executors.newFixedThreadPool(activeConnectionThreads);

    private final Thread serverThread;


    public Server() {
        serverThread = new Thread(this::init);
    }

    public void start(){
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        serverThread.start();
    }

    private void init() {
        try {
            while (true) {
                synchronized (this) {
                    if (activeCount > activeConnectionThreads) this.wait();
                    new ConnectionHandler(this);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public ExecutorService getService() {
        return service;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void addCount() {
        activeCount++;
    }

    public void removeCount(){
        activeCount--;
    }

    public void listen(int port){
        this.port = port;
    }

    public void addHandler(Query query, String path, Handler handler){
        query.addToMap(path, handler);
    }
}
