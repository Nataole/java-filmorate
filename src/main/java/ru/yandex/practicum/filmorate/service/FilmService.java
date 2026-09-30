package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    public FilmService(FilmStorage filmStorage,
                       UserStorage userStorage,
                       GenreStorage genreStorage,
                       MpaStorage mpaStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    public Film create(Film film) {
        checkMpaAndGenres(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            throw new NotFoundException("Фильм не найден");
        }

        getById(film.getId());
        checkMpaAndGenres(film);

        return filmStorage.update(film);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(Long id) {
        return filmStorage.getById(id)
                .orElseThrow(() ->
                        new NotFoundException("Фильм с id=" + id + " не найден"));
    }

    public Film addLike(Long filmId, Long userId) {
        getById(filmId);
        checkUserExists(userId);

        filmStorage.addLike(filmId, userId);

        return getById(filmId);
    }

    public Film removeLike(Long filmId, Long userId) {
        getById(filmId);
        checkUserExists(userId);

        filmStorage.removeLike(filmId, userId);

        return getById(filmId);
    }

    public List<Film> getPopularFilms(Integer count) {
        if (count == null) {
            count = 10;
        }

        if (count <= 0) {
            throw new ValidationException(
                    "Количество фильмов должно быть положительным"
            );
        }

        return filmStorage.getPopularFilms(count);
    }

    private void checkUserExists(Long userId) {
        userStorage.getById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Пользователь с id=" + userId + " не найден"
                        ));
    }

    private void checkMpaAndGenres(Film film) {
        if (film.getMpa() != null) {
            Integer mpaId = film.getMpa().getId();

            if (mpaId == null) {
                throw new NotFoundException("Рейтинг MPA не найден");
            }

            mpaStorage.getById(mpaId)
                    .orElseThrow(() ->
                            new NotFoundException(
                                    "Рейтинг MPA с id=" + mpaId + " не найден"
                            ));
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (genreIds.contains(null)) {
                throw new NotFoundException("Жанр не найден");
            }

            List<Genre> genres = genreStorage.getByIds(genreIds);

            if (genres.size() != genreIds.size()) {
                throw new NotFoundException(
                        "Один или несколько жанров не найдены"
                );
            }

            film.setGenres(new LinkedHashSet<>(genres));
        }
    }
}

