package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MovieStore;

import java.io.IOException;
import java.net.InetSocketAddress;

class MoviesServer {
    private final HttpServer server;

    public MoviesServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            MovieStore moviesStore = new MovieStore();

            server.createContext("/movies", new BaseHttpHandler(moviesStore));

        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать HTTP-сервер", e);
        }
    }

    public void start() {
        server.start();
        System.out.println("Сервер запущен");
    }

    public void stop() {
        server.stop(0);
        System.out.println("Сервер остановлен");
    }
}
