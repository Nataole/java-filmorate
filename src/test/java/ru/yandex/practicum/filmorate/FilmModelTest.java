package ru.yandex.practicum.filmorate;

import ru.yandex.practicum.filmorate.model.Film;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FilmModelTest {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldValidateCorrectFilm() {
        Film film = new Film(
                null,
                "Film",
                "Description",
                LocalDate.of(2000, 1, 1),
                120L,
                 new HashSet<>()
        );

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldNotValidateFilmWithBlankName() {
        Film film = new Film(
                null,
                "",
                "Description",
                LocalDate.of(2000, 1, 1),
                120L,
                new HashSet<>()
        );

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldNotValidateFilmWithLongDescription() {
        Film film = new Film(
                null,
                "Film",
                "a".repeat(201),
                LocalDate.of(2000, 1, 1),
                120L,
                new HashSet<>()
        );

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldValidateFilmWithDescriptionLength200() {
        Film film = new Film(
                null,
                "Film",
                "a".repeat(200),
                LocalDate.of(2000, 1, 1),
                100L,
                new HashSet<>()
        );

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldNotValidateFilmWithTooOldReleaseDate() {
        Film film = new Film(
                null,
                "Film",
                "Description",
                LocalDate.of(1895, 12, 27),
                120L,
                new HashSet<>()
        );

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldValidateFilmWithReleaseDateOnBoundary() {
        Film film = new Film(
                null,
                "Film",
                "Description",
                LocalDate.of(1895, 12, 28),
                120L,
                new HashSet<>()
        );

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty());
    }


    @Test
    void shouldNotValidateFilmWithNegativeDuration() {
        Film film = new Film(
                null,
                "Film",
                "Description",
                LocalDate.of(2000, 1, 1),
                -1L,
                new HashSet<>()
        );

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldNotValidateFilmWithZeroDuration() {
        Film film = new Film(
                null,
                "Film",
                "Description",
                LocalDate.of(2000, 1, 1),
                0L,
                new HashSet<>()
        );

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
    }

}

