package ru.vixtor;

import ru.vixtor.server.Handler;
import ru.vixtor.server.Method;
import ru.vixtor.server.Request;
import ru.vixtor.server.Server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/events.html", "/events.js");

    public static void main(String[] args) {
        Server server = new Server();
        // код инициализации сервера (из вашего предыдущего ДЗ)

        // добавление хендлеров (обработчиков)
        server.addHandler(Method.GET, "/classic.html", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                try {
                    String template = Files.readString(request.getFilePath());
                    byte[] content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();

                    responseStream.write(request.getResponse(content).getBytes());
                    responseStream.write(content);
                    responseStream.flush();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        server.addHandler(Method.GET, "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                // TODO: handlers code
            }
        });
        server.addHandler(Method.POST, "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                // TODO: handlers code
            }
        });

        handlersReg(server);

        server.listen(9999);
        server.start();
    }


    private static void handlersReg(Server server){
        for (String path: validPaths){
            server.addHandler(Method.GET, path, new Handler());
        }
    }
}


