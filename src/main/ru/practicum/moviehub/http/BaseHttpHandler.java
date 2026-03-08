package ru.practicum.moviehub.http;

import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import ru.practicum.moviehub.store.MovieStore;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.api.ErrorsHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseHttpHandler extends MoviesHttpHandler {
    Gson gsonDefault = new Gson();
    Gson gsonMovieAdapter = new GsonBuilder()
            .registerTypeAdapter(Movie.class, new MovieAdapter())
            .create();
    private final MovieStore moviesStore;
    private final ErrorsHandler errorsHandler;

    public BaseHttpHandler(MovieStore moviesStore) {
        this.moviesStore = moviesStore;
        this.errorsHandler = new ErrorsHandler(this::sendJson);
    }

    public void handle(HttpExchange ex) throws IOException {
        String qery = ex.getRequestURI().getQuery();

        String method = ex.getRequestMethod();
        if (method.equalsIgnoreCase("GET")) {
            if (path.matches("/movies/\\d+")) {
                handleGetById(ex, path);
            } else if (path.equals("/movies")) {
                if (moviesStore.getListOfMovies().isEmpty()) {
                    sendJson(ex, 200, "[]");
                } else {
                    String gsonListOfMovies = gsonDefault.toJson(moviesStore.getListOfMovies());
                    sendJson(ex, 200, gsonListOfMovies);
                }
            } else if (path.equals("/movie") && qery != null && qery.contains("year")) {
                handleGetMoviesByDate(ex, qery);
            } else {
                errorsHandler.methodNotAllowed(ex);
            }

            String requestBody = readRequestBody(ex);
            try {
                Movie movie = gsonMovieAdapter.fromJson(requestBody, Movie.class);
                if (movie.getName().length() > 100) {
                    errorsHandler.validationError(ex, "Name should contain less than 100 symbols");
                } else if (movie.getYearOfRelease() < 1888 || movie.getYearOfRelease() > 2026) {
                    errorsHandler.validationError(ex, "Year should be between 1888 and 2026");
                } else {
                    moviesStore.addMovie(movie);
                    sendJson(ex, 201, gsonMovieAdapter.toJson(movie));
                }
            } catch (IOException exception) {
                errorsHandler.validationError(ex);
            }
        } else if (method.equalsIgnoreCase("DELETE")) {
            if (path.matches("/movies/\\d+")) {
                handleDeleteById(ex, path);
            } else {
                errorsHandler.methodNotAllowed(ex);
            }
        } else {
            errorsHandler.methodNotAllowed(ex);
        }
    }
    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (var reader = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {

            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            return body.toString();
        }
    }

    private void handleGetById(HttpExchange exchange, String path) throws IOException {
        try {
            String idStr = path.substring(path.lastIndexOf("/") + 1);
            int id = Integer.parseInt(idStr);
            Movie movie = moviesStore.searchMovie(id);
            if (movie == null) {
                errorsHandler.movieNotFound(exchange);
                return;
            }
            sendJson(exchange, 200, gsonMovieAdapter.toJson(movie));
        } catch (NumberFormatException e) {
            errorsHandler.invalidIdFormat(exchange);
        }
    }
    private void handleDeleteById(HttpExchange exchange, String path) throws IOException {
        try {
            String idStr = path.substring(path.lastIndexOf("/") + 1);
            int id = Integer.parseInt(idStr);
            if (moviesStore.removeMovie(id)) {
                sendNoContent(exchange);
            } else {
                errorsHandler.movieNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            errorsHandler.invalidIdFormat(exchange);
        }
    }

    private void handleGetMoviesByDate(HttpExchange ex, String query) throws IOException {
        Map<String, String> params = parseQuery(query);
        if (params.get("year") == null || params.get("year").trim().isEmpty()) {
            errorsHandler.yearRequired(ex);
            return;
        }
        try {
            int dateStr = Integer.parseInt(params.get("year"));

            List<Movie> result = moviesStore.getListOfMovies().stream()
                    .filter(movie -> movie.getYearOfRelease() == dateStr)
                    .toList();

            String json = gsonDefault.toJson(result);
            sendJson(ex, 200, json);
        } catch (NumberFormatException e) {
            errorsHandler.yearMustBeNumber(ex);
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = pair.substring(idx + 1);
                result.put(key, value);
            }
        }
        return result;
    }
}
