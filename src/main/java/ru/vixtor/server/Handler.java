package ru.vixtor.server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class Handler {

    public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
        responseStream.write(request.getResponse().getBytes());
        Files.copy(request.getFilePath(), responseStream);
        responseStream.flush();
    }
}
