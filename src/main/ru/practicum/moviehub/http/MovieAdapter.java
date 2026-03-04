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

        jw.beginObject();  // Начинаем запись JSON-объекта { ... }

        jw.name("name");          // Ключ: "name"
        jw.value(movie.getName()); // Значение: строка

        jw.name("yearOfRelease");  // Ключ: "yearOfRelease"
        jw.value(movie.getYearOfRelease()); // Значение: число

        jw.endObject();  // Завершаем объект
    }
    public Movie read(final JsonReader jr) throws IOException {
        jr.beginObject();  // Начинаем чтение объекта

        String name = null;
        int yearOfRelease = 0;

        while (jr.hasNext()) {
            String fieldName = jr.nextName();  // Получаем имя поля

            switch (fieldName) {
                case "name":
                    name = jr.nextString();
                    break;
                case "yearOfRelease":
                    yearOfRelease = jr.nextInt();
                    break;
                default:
                    jr.skipValue();  // Пропускаем неизвестные поля
                    break;
            }
        }

        jr.endObject();  // Завершаем чтение объекта

        // Создаём и возвращаем объект Movie
        return new Movie(name, yearOfRelease);
    }
}
