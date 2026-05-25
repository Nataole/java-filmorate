package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import jakarta.validation.Valid;
import ru.yandex.practicum.filmorate.service.FilmService;


import java.util.List;


@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {

        Film createdFilm = filmService.create(film);
        log.info("Фильм добавлен: {}", createdFilm);
        return createdFilm;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        Film updatedFilm = filmService.update(film);
        log.info("Фильм обновлен: {}", updatedFilm);
        return updatedFilm;
    }

    @GetMapping
    public List<Film> getAll() {
        log.info("Получен список фильмов");
        return filmService.getAll();
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") Integer count) {
        log.info("Получение популярных фильмов, count={}", count);
        return filmService.getPopularFilms(count);
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable Long id) {
        Film film = filmService.getById(id);

        log.info("Получен фильм с id={}", id);
        return film;
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь id={} поставил лайк фильму id={}", userId, id);
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь id={} удалил лайк у фильма id={}", userId, id);
        return filmService.removeLike(id, userId);
    }
}

