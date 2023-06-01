package ru.vixtor.server;

import org.apache.commons.fileupload.FileUploadException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
        try (BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            // лимит на request line + заголовки
            final var limit = 4096;

            in.mark(limit);
            byte[] buffer = new byte[limit];
            int read = in.read(buffer);

            // ищем request line
            byte[] requestLineDelimiter = new byte[]{'\r', '\n'};
            int requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
            if (requestLineEnd == -1) {
                badRequest(out);
                return;
            }

            // читаем request line
            String[] requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
            if (requestLine.length != 3) {
                badRequest(out);
                return;
            }

            Method method;

            try {
                method = Method.valueOf(requestLine[0]);
            } catch (IllegalArgumentException ignored) {
                badRequest(out);
                return;
            }
            System.out.println(method);

            String path = requestLine[1];
            if (!path.startsWith("/")) {
                badRequest(out);
                return;
            }
            System.out.println(path);

            // ищем заголовки
            byte[] headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
            int headersStart = requestLineEnd + requestLineDelimiter.length;
            int headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
            if (headersEnd == -1) {
                badRequest(out);
                return;
            }

            // отматываем на начало буфера
            in.reset();
            // пропускаем requestLine
            in.skip(headersStart);

            byte[] headersBytes = in.readNBytes(headersEnd - headersStart);
            List<String> headers = Arrays.asList(new String(headersBytes).split("\r\n"));
            System.out.println(headers);

            String body = "";

            // для GET тела нет
            if (!method.equals(Method.GET)) {
                in.skip(headersDelimiter.length);
                // вычитываем Content-Length, чтобы прочитать body
                Optional<String> contentLength = extractHeader(headers, "Content-Length");
                if (contentLength.isPresent()) {
                    int length = Integer.parseInt(contentLength.get());
                    byte[] bodyBytes = in.readNBytes(length);

                    body = new String(bodyBytes);
                    System.out.println(body);
                }
            }

            String toHandle;

            if (path.contains("?")) {
                toHandle = path.substring(0, path.indexOf("?"));
            } else {
                toHandle = path;
            }

            if (!method.getMap().containsKey(toHandle)) {
                notFound(out);
                return;
            }

            Handler handler = method.getMap().get(toHandle);

            Optional<String> contentType = extractHeader(headers, "Content-Type");

            Request request;

            if (contentType.isPresent() && contentType.get().startsWith("multipart/form-data")) {
                request = new MultiPartRequest(method, toHandle, path, body);
            } else {
                request = new Request(method, toHandle, path, body);
            }
            handler.handle(request, out);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (FileUploadException e) {
            throw new RuntimeException(e);
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

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }
}
