package ru.vixtor.server;

import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private int port = 9999;

    private final ExecutorService service = Executors.newFixedThreadPool(64);

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();

                service.execute(() -> connection(socket));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen(int port) {
        this.port = port;
    }

    public void addHandler(Method method, String path, Handler handler) {
        method.addToMap(path, handler);
    }

    private void connection(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            String requestLine = in.readLine();
            if(requestLine == null)return;


            String[] parts = requestLine.split(" ");

            if (parts.length != 3) return;

            String path = parts[1];
            Method method = Method.valueOf(parts[0]);

            String toHandle;

            if(path.contains("?")){
                toHandle = path.substring(0, path.indexOf("?"));
            }else {
                toHandle = path;
            }

            if (!method.getMap().containsKey(toHandle)) {
                notFound(out);
                return;
            }

            Handler handler = method.getMap().get(toHandle);

            Request request = new Request(method, toHandle, path);

            handler.handle(request, out);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +

                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }
}
