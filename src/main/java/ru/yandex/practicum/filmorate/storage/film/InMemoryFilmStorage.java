package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1;

    @Override
    public Film create(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> getById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        films.get(filmId).getLikes().add(userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        films.get(filmId).getLikes().remove(userId);
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        return films.values().stream()
                .sorted((film1, film2) ->
                        Integer.compare(
                                film2.getLikes().size(),
                                film1.getLikes().size()
                        )
                )
                .limit(count)
                .toList();
    }
}
