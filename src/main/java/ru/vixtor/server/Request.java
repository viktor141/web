package ru.vixtor.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Request {

    private String path;
    private Path filePath;
    private String mimeType;
    private long length;

    private String response;

    public Request(Query query, String path){
        this.path = path;
        filePath = Path.of(".", "public", path);
    }

    public String getResponse() {
        try {
            mimeType = Files.probeContentType(filePath);

            length = Files.size(filePath);

        }catch (IOException e){
            e.printStackTrace();
        }
        response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        return response;
    }

    public String getResponse(byte[] content){
        try {
            mimeType = Files.probeContentType(filePath);

            length = Files.size(filePath);

        }catch (IOException e){
            e.printStackTrace();
        }
        response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + content.length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        return response;
    }

    public Path getFilePath() {
        return filePath;
    }
}
