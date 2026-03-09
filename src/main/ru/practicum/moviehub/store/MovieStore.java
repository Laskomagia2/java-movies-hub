package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.HashMap;

public class MovieStore {
    private final HashMap<Integer, Movie> listOfMovies = new HashMap<Integer, Movie>();

    public ArrayList<Movie> getListOfMovies() {
        return new ArrayList<>(listOfMovies.values());
    }

    public void addMovie(Movie movie) {
        listOfMovies.put(movie.getMovieId(), movie);
    }

    public Movie searchMovie(int movieId) {
        return listOfMovies.get(movieId);
    }

    public boolean removeMovie(int movieId) {
        if (listOfMovies.containsKey(movieId)) {
            listOfMovies.remove(movieId);
            return true;
        } else {
            return false;
        }
    }

    public boolean isMovieExist(int movieId) {
        return listOfMovies.containsKey(movieId);
    }
}
