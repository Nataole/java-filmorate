package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> getAll() {
        String sql = """
                SELECT id, name
                FROM mpa
                ORDER BY id
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Mpa mpa = new Mpa();
                    mpa.setId(rs.getInt("id"));
                    mpa.setName(rs.getString("name"));
                    return mpa;
                }
        );
    }

    @Override
    public Optional<Mpa> getById(Integer id) {
        String sql = """
                SELECT id, name
                FROM mpa
                WHERE id = ?
                """;

        List<Mpa> ratings = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Mpa mpa = new Mpa();
                    mpa.setId(rs.getInt("id"));
                    mpa.setName(rs.getString("name"));
                    return mpa;
                },
                id
        );

        return ratings.stream().findFirst();
    }
}
