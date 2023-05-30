package ru.vixtor.server;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Request {

    private final Method method;
    private Path filePath;
    private String mimeType;
    private long length;

    private String response;

    private final Map<String, String> params = new HashMap<>();

    private final Map<String, String> bodyParams = new HashMap<>();


    public Request(Method method, String handled, String path, String body) {
        this.method = method;
        filePath = Path.of(".", "public", handled);

        try {
            List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(new URI(path), StandardCharsets.UTF_8);

            for (NameValuePair nameValuePair : nameValuePairs) {
                params.put(nameValuePair.getName(), nameValuePair.getValue());
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if(method.equals(Method.POST)) {
            for (String part : body.split("&")) {
                String[] keyAndValue = part.split("=");
                bodyParams.put( (!bodyParams.containsKey(keyAndValue[0])) ? keyAndValue[0] : keyAndValue[0] + "*Duplicated", (keyAndValue.length > 1) ? keyAndValue[1] : null);
            }
        }
    }

    public String getResponse() {
        try {
            mimeType = Files.probeContentType(filePath);

            length = Files.size(filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
        response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        return response;
    }

    public String getResponse(byte[] content) {
        try {
            mimeType = Files.probeContentType(filePath);

            length = Files.size(filePath);

        } catch (IOException e) {
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

    public String getQueryParam(String name) {
        return params.get(name);
    }

    public Map<String, String> getQueryParams() {
        return params;
    }

    public String getPostParam(String name) {
        return bodyParams.get(name);
    }

    public Map<String, String> getPostParams() {
        return bodyParams;
    }
}
