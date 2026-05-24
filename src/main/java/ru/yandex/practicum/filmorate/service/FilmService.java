package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;


import java.util.List;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(Long id) {
        return filmStorage.getById(id);
    }

    public Film addLike(Long filmId, Long userId) {
        Film film = getById(filmId);

        if (film == null || userStorage.getById(userId) == null) {
            throw new NotFoundException(
                    "Фильм с id=" + filmId
                            + " или пользователь с id=" + userId
                            + " не найден"
            );
        }

        film.getLikes().add(userId);
        return film;
    }

    public Film removeLike(Long filmId, Long userId) {
        Film film = getById(filmId);

        if (film == null || userStorage.getById(userId) == null) {
            throw new NotFoundException(
                    "Фильм с id=" + filmId
                            + " или пользователь с id=" + userId
                            + " не найден"
            );
        }

        film.getLikes().remove(userId);
        return film;
    }

    public List<Film> getPopularFilms(Integer count) {
        if (count == null) {
            count = 10;
        }

        return filmStorage.getAll().stream()
                .sorted((f1, f2) ->
                        Integer.compare(f2.getLikes().size(),
                                f1.getLikes().size()))
                .limit(count)
                .toList();
    }
}
