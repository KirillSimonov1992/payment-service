package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HttpServer {

    private static final String NAME_OF_MODULE = "\\simple-http-server\\src\\main\\java\\";
    private static final String ABSOLUTE_PATH_OF_PROJECT = System.getProperty("user.dir");
    private static final String DEFAULT_FOLDER = "static";
    private static final String DEFAULT_EXTENSION = "html";
    private static final Integer port = 8080;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started at http://localhost:8080");

        while (true) {

            Socket clientSocket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            String response = "";
            String line;
            int i = 0;

            while ((line = in.readLine()) != null && !line.isEmpty()) {
                System.out.println(line);
                if (i++ == 0) {
                    String nameOfFile = line.split(" ")[1];
                    String nameOfFolder;
                    nameOfFolder = (args.length != 0 ? args[0] : DEFAULT_FOLDER);
                    String path = ABSOLUTE_PATH_OF_PROJECT + NAME_OF_MODULE + nameOfFolder + nameOfFile;
                    String ext = nameOfFile.split("\\.")[1];
                    Path p = Paths.get(path);
                    if (Files.exists(p) && ext.equals(DEFAULT_EXTENSION)) {
                        response = generateResponseOk(p);
                    } else {
                        response = generateResponseNotFound();
                    }
                }
            }
            out.write(response);
            out.flush();
            clientSocket.close();
        }
    }

    private static String generateResponseOk(Path path) throws IOException {
        String lineOfFile;
        StringBuilder response = new StringBuilder();
        StringBuilder text = new StringBuilder();

        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(Files.readAllBytes(path))));
        while ((lineOfFile = br.readLine()) != null && !lineOfFile.isEmpty()) {
            text.append(lineOfFile);
        }
        response.append("HTTP/1.1 200 OK\r\n")
                .append("Content-Type: text/html; charset=UTF-8\r\n")
                .append("Content-Length: " + Files.size(path) + "\r\n")
                .append("\r\n")
                .append("<h1>")
                .append(text)
                .append("</h1>");

        return response.toString();
    }
    private static String generateResponseNotFound() {
        return "HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "\r\n" +
                "<html>\r\n" +
                "<head><title>404 Not Found</title></head>\r\n" +
                "<body>\r\n" +
                "<h1>Not Found</h1>\r\n" +
                "<p>The requested URL was not found on this server.</p>\r\n" +
                "</body>\r\n" +
                "</html>\r\n";
    }
}

