package ru.practicum.moviehub.api;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class ErrorsHandler {

    private final JsonSender sender;

    @FunctionalInterface
    public interface JsonSender {
        void send(HttpExchange ex, int status, String json) throws IOException;
    }

    public ErrorsHandler(JsonSender sender) {
        this.sender = sender;
    }

    public void methodNotAllowed(HttpExchange ex) throws IOException {
        sender.send(ex, 405, "{\"error\": \"Method not allowed\"}");
    }

    public void validationError(HttpExchange ex) throws IOException {
        sender.send(ex, 422, "{\"error\": \"Error of validation\"}");
    }

    public void validationError(HttpExchange ex, String caseMessage) throws IOException {
        String json = String.format("{\"error\": \"Error of validation\", \"case\": \"%s\"}",
                escapeJson(caseMessage));
        sender.send(ex, 422, json);
    }

    public void movieNotFound(HttpExchange ex) throws IOException {
        sender.send(ex, 404, "{\"error\": \"Movie not found\"}");
    }

    public void invalidIdFormat(HttpExchange ex) throws IOException {
        sender.send(ex, 400, "{\"error\": \"Invalid ID format\"}");
    }

    public void yearRequired(HttpExchange ex) throws IOException {
        sender.send(ex, 400, "{\"error\": \"Parametr 'year' is required\"}");
    }

    public void yearMustBeNumber(HttpExchange ex) throws IOException {
        sender.send(ex, 400, "{\"error\": \"Parametr 'year' must be a number\"}");
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
