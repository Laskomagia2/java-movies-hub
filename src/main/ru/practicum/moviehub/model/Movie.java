package ru.practicum.moviehub.model;

public class Movie {
    public Movie(String name, int yearOfRelease) {
        this.name = name;
        this.yearOfRelease = yearOfRelease;
        this.movieId = this.hashCode();
    }
    private final String name;
    private final int yearOfRelease;
    private final int movieId;

    public String getName() {
        return name;
    }
    public int getYearOfRelease() {
        return yearOfRelease;
    }

    public int getMovieId() {
        return movieId;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        if (name != null) {
            hash = name.hashCode();
        }
        hash += yearOfRelease;
        return hash;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return yearOfRelease == movie.yearOfRelease && name.equals(movie.name);
    }

}
