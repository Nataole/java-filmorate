package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;

@Repository("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film create(Film film) {
        String sql = """
                INSERT INTO films (
                    name,
                    description,
                    release_date,
                    duration,
                    mpa_id
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setDate(3, Date.valueOf(film.getReleaseDate()));
            statement.setLong(4, film.getDuration());

            if (film.getMpa() != null && film.getMpa().getId() != null) {
                statement.setInt(5, film.getMpa().getId());
            } else {
                statement.setNull(5, Types.INTEGER);
            }

            return statement;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());

        saveGenres(film);

        return getById(film.getId()).orElse(film);
    }

    @Override
    public Film update(Film film) {
        String sql = """
                UPDATE films
                SET name = ?,
                    description = ?,
                    release_date = ?,
                    duration = ?,
                    mpa_id = ?
                WHERE id = ?
                """;

        Integer mpaId = film.getMpa() == null
                ? null
                : film.getMpa().getId();

        jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mpaId,
                film.getId()
        );

        jdbcTemplate.update(
                "DELETE FROM film_genres WHERE film_id = ?",
                film.getId()
        );

        saveGenres(film);

        return getById(film.getId()).orElse(film);
    }

    @Override
    public List<Film> getAll() {
        String sql = """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.mpa_id,
                       m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                ORDER BY f.id
                """;

        List<Film> films = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapRowToFilm(rs)
        );

        films.forEach(this::loadFilmRelations);

        return films;
    }

    @Override
    public Optional<Film> getById(Long id) {
        String sql = """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.mpa_id,
                       m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                WHERE f.id = ?
                """;

        List<Film> films = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapRowToFilm(rs),
                id
        );

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.getFirst();

        loadFilmRelations(film);

        return Optional.of(film);
    }

    private Film mapRowToFilm(java.sql.ResultSet rs)
            throws java.sql.SQLException {

        Film film = new Film();

        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(
                rs.getDate("release_date").toLocalDate()
        );
        film.setDuration(rs.getLong("duration"));

        Integer mpaId = rs.getObject("mpa_id", Integer.class);

        if (mpaId != null) {
            Mpa mpa = new Mpa();
            mpa.setId(mpaId);
            mpa.setName(rs.getString("mpa_name"));

            film.setMpa(mpa);
        }

        film.setLikes(new HashSet<>());
        film.setGenres(new HashSet<>());

        return film;
    }

    private void loadFilmRelations(Film film) {
        film.setLikes(getLikes(film.getId()));
        film.setGenres(getGenres(film.getId()));
    }

    private Set<Long> getLikes(Long filmId) {
        String sql = """
                SELECT user_id
                FROM likes
                WHERE film_id = ?
                """;

        return new HashSet<>(
                jdbcTemplate.query(
                        sql,
                        (rs, rowNum) -> rs.getLong("user_id"),
                        filmId
                )
        );
    }

    private Set<Genre> getGenres(Long filmId) {
        String sql = """
                SELECT g.id, g.name
                FROM genres g
                JOIN film_genres fg ON g.id = fg.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """;

        return new LinkedHashSet<>(jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(rs.getInt("id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                },
                filmId
        ));
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO film_genres (film_id, genre_id)
                VALUES (?, ?)
                """;

        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(
                    sql,
                    film.getId(),
                    genre.getId()
            );
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = """
                INSERT INTO likes (film_id, user_id)
                VALUES (?, ?)
                """;

        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = """
                DELETE FROM likes
                WHERE film_id = ? AND user_id = ?
                """;

        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        String sql = """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.mpa_id,
                       m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                LEFT JOIN likes l ON f.id = l.film_id
                GROUP BY f.id,
                         f.name,
                         f.description,
                         f.release_date,
                         f.duration,
                         f.mpa_id,
                         m.name
                ORDER BY COUNT(l.user_id) DESC, f.id
                LIMIT ?
                """;

        List<Film> films = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapRowToFilm(rs),
                count
        );

        films.forEach(this::loadFilmRelations);

        return films;
    }
}