package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import(FilmDbStorage.class)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    @Autowired
    FilmDbStorageTest(FilmDbStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    @Test
    void shouldCreateAndFindFilmById() {
        Film film = createFilm();

        Film created = filmStorage.create(film);

        assertNotNull(created.getId());

        Film found = filmStorage.getById(created.getId())
                .orElseThrow();

        assertEquals(created.getId(), found.getId());
        assertEquals("Test Film", found.getName());
        assertEquals("Test Description", found.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), found.getReleaseDate());
        assertEquals(120L, found.getDuration());

        assertNotNull(found.getMpa());
        assertEquals(1, found.getMpa().getId());

        assertEquals(1, found.getGenres().size());
        assertTrue(
                found.getGenres().stream()
                        .anyMatch(genre -> genre.getId().equals(1))
        );
    }

    @Test
    void shouldReturnEmptyForUnknownFilm() {
        assertTrue(
                filmStorage.getById(999L).isEmpty()
        );
    }

    @Test
    void shouldUpdateFilm() {
        Film film = filmStorage.create(createFilm());

        film.setName("Updated Film");
        film.setDescription("Updated Description");
        film.setDuration(150L);

        Film updated = filmStorage.update(film);

        assertEquals("Updated Film", updated.getName());
        assertEquals("Updated Description", updated.getDescription());
        assertEquals(150L, updated.getDuration());
    }

    private Film createFilm() {
        Mpa mpa = new Mpa();
        mpa.setId(1);

        Genre genre = new Genre();
        genre.setId(1);

        HashSet<Genre> genres = new HashSet<>();
        genres.add(genre);

        Film film = new Film();

        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);
        film.setMpa(mpa);
        film.setGenres(genres);
        film.setLikes(new HashSet<>());

        return film;
    }
}
