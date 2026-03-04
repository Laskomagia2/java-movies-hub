package ru.practicum.moviehub.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ru.practicum.moviehub.model.Movie;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;
    private static Movie testMovie;
    private final Gson gsonMovies =
            new GsonBuilder().registerTypeAdapter(Movie.class, new MovieAdapter()).create();
    private final Gson gsonDefault = new Gson();

    @BeforeAll
    static void beforeAll() {
        server = new MoviesServer();
        server.start();
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    @Order(1)
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    @Order(2)
    void postMovie_createsMovie_andReturns201WithBody() throws IOException, InterruptedException {
        testMovie = new Movie("testMovie", 1955);
        String movieToJson = gsonMovies.toJson(testMovie);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(movieToJson, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201");

        Movie returned = gsonMovies.fromJson(resp.body(), Movie.class);
        assertEquals(testMovie.getName(), returned.getName());
        assertEquals(testMovie.getYearOfRelease(), returned.getYearOfRelease());
    }

    @Test
    @Order(3)
    void getMovieById_whenExists_returnsMovie() throws Exception {
        int id = testMovie.getMovieId();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + id))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies/{id} для существующего фильма должен вернуть 200");

        Movie returned = gsonMovies.fromJson(resp.body(), Movie.class);
        assertEquals(testMovie.getName(), returned.getName());
        assertEquals(testMovie.getYearOfRelease(), returned.getYearOfRelease());
    }

    @Test
    @Order(4)
    void getMoviesByYear_returnsOnlyMoviesOfThatYear() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movie?year=" + testMovie.getYearOfRelease()))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movie?year=... должен вернуть 200");

        var listType = new TypeToken<java.util.List<Movie>>() {}.getType();
        java.util.List<Movie> movies = gsonDefault.fromJson(resp.body(), listType);
        assertTrue(movies.stream().allMatch(m -> m.getYearOfRelease() == testMovie.getYearOfRelease()));
    }

    @Test
    @Order(5)
    void deleteMovieById_whenExists_returns204() throws Exception {
        int id = testMovie.getMovieId();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + id))
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, resp.statusCode(), "DELETE /movies/{id} для существующего фильма должен вернуть 204");
        assertTrue(resp.body() == null || resp.body().isBlank(), "Тело ответа при 204 должно быть пустым или отсутствовать");
    }

    @Test
    @Order(6)
    void getMovieById_whenNotExists_returns404() throws Exception {
        int unknownId = testMovie.getMovieId();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + unknownId))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode(), "GET /movies/{id} для несуществующего фильма должен вернуть 404");
    }
}
