package org.apache.commons.codec.demo;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.apache.commons.codec.CodecPolicy;
import org.apache.commons.codec.binary.Base64;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Demo {
    private static final int PORT = 8080;
    private static final String CHAR_ENCODING = "UTF-8";

    public static void main(String[] args) throws IOException {
        final HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        System.out.println("Server running on http://localhost:" + PORT);

        server.createContext("/", new HtmlHandler());

        server.createContext("/encode", new EncodeHandler());

        server.setExecutor(null); // Usa il default executor
        server.start();
    }

    static class HtmlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            final String htmlContent = readHtmlFile("/Users/leonardofalanga/Downloads/" +
                    "commons-codec/src/main/java/org/apache/commons/codec/exampleDocker/index.html");

            exchange.getResponseHeaders().add("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, htmlContent.getBytes(CHAR_ENCODING).length);

            final OutputStream os = exchange.getResponseBody();
            os.write(htmlContent.getBytes(CHAR_ENCODING));
            os.close();
        }

        private String readHtmlFile(String fileName) throws IOException {
            final BufferedReader reader = new BufferedReader(new FileReader(fileName));
            final StringBuilder htmlContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                htmlContent.append(line).append("\n");
            }
            reader.close();
            return htmlContent.toString();

        }
    }


    static class EncodeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                final InputStream input = exchange.getRequestBody();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(input, CHAR_ENCODING));
                final StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }

                try {
                    final Base64 codec = new Base64(0, null, false, CodecPolicy.STRICT);
                    final byte[] response = codec.encode(requestBody.toString().getBytes(CHAR_ENCODING));
                    exchange.getResponseHeaders().add("Content-Type", "text/plain");
                    exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                    exchange.sendResponseHeaders(200, response.length);
                    exchange.getResponseBody().write(response);
                } catch (Exception e) {
                    final String error = "Error: " + e.getMessage();
                    exchange.sendResponseHeaders(500, error.length());
                    exchange.getResponseBody().write(error.getBytes());
                } finally {
                    exchange.getResponseBody().close();
                }
            } else {
                final String error = "Method not allowed";
                exchange.sendResponseHeaders(405, error.length());
                exchange.getResponseBody().write(error.getBytes());
                exchange.getResponseBody().close();
            }
        }
    }
}