package ru.practicum.moviehub.http;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import ru.practicum.moviehub.model.Movie;

import java.io.IOException;

public class MovieAdapter extends TypeAdapter<Movie> {
    @Override
    public void write(final JsonWriter jw, final Movie movie) throws IOException {
        if (movie == null) {
            jw.nullValue();
            return;
        }

        jw.beginObject();

        jw.name("name");
        jw.value(movie.getName());

        jw.name("yearOfRelease");
        jw.value(movie.getYearOfRelease());

        jw.endObject();
    }

    public Movie read(final JsonReader jr) throws IOException {
        jr.beginObject();

        String name = null;
        int yearOfRelease = 0;

        while (jr.hasNext()) {
            String fieldName = jr.nextName();

            switch (fieldName) {
                case "name":
                    name = jr.nextString();
                    break;
                case "yearOfRelease":
                    yearOfRelease = jr.nextInt();
                    break;
                default:
                    jr.skipValue();
                    break;
            }
        }

        jr.endObject();

        return new Movie(name, yearOfRelease);
    }
}
