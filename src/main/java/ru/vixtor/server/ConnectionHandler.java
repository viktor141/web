package ru.vixtor.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ConnectionHandler {

    private final Server server;


    public ConnectionHandler(Server server) {
        this.server = server;

        connection();
    }
    private void connection(){
        server.addCount();
        server.getService().submit(() -> {
            try (
                    Socket socket = server.getServerSocket().accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            ) {
                String requestLine = in.readLine();
                String[] parts = requestLine.split(" ");

                if (parts.length != 3) return;

                String path = parts[1];
                Query query = Query.valueOf(parts[0]);

                if (!query.getMap().containsKey(path)) {
                    out.write((
                            "HTTP/1.1 404 Not Found\r\n" +
                                    "Content-Length: 0\r\n" +

                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.flush();
                    return;
                }

                Handler handler = query.getMap().get(path);

                Request request = new Request(query, path);

                handler.handle(request, out);

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                closeConnection();
            }
        });
    }

    private void closeConnection(){
        synchronized (server) {
            server.removeCount();
            server.notify();
        }
    }


}
